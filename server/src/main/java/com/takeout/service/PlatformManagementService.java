package com.takeout.service;

import com.takeout.common.*;
import com.takeout.domain.Order;
import com.takeout.domain.enums.PayStatus;
import com.takeout.dto.admin.*;
import com.takeout.exception.BusinessException;
import com.takeout.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.takeout.security.AdminContext;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.*;

@Service
public class PlatformManagementService {
    private final AppUserRepository users; private final ShopRepository shops; private final OrderRepository orders;
    private final AdminAccountRepository admins; private final OrderItemRepository orderItems; private final SystemConfigRepository configs; private final RefundRepository refunds;
    private final AuditLogRepository audits; private final PasswordEncoder passwordEncoder; private final AdminContext adminContext;
    private final InventoryService inventoryService; private final RefundService refundService;
    public PlatformManagementService(AppUserRepository users, ShopRepository shops, OrderRepository orders, AdminAccountRepository admins,
      OrderItemRepository orderItems, SystemConfigRepository configs, AuditLogRepository audits, PasswordEncoder passwordEncoder, AdminContext adminContext, InventoryService inventoryService, RefundService refundService, RefundRepository refunds) {
      this.users=users; this.shops=shops; this.orders=orders; this.admins=admins; this.orderItems=orderItems; this.configs=configs; this.audits=audits; this.passwordEncoder=passwordEncoder; this.adminContext=adminContext; this.inventoryService=inventoryService; this.refundService=refundService; this.refunds=refunds;
    }
    public PlatformOverviewResponse overview() {
        List<Order> all=orders.findByIsDeletedOrderByCreatedAtDesc(0);
        BigDecimal sales=all.stream().filter(o->o.getPayStatus().equals(PayStatus.PAID.getCode())).map(Order::getPayAmount).reduce(BigDecimal.ZERO,BigDecimal::add);
        return new PlatformOverviewResponse(users.count(),shops.count(),all.size(),sales);
    }
    public PageResult<PlatformUserResponse> listUsers(String keyword,Integer status,long page,long size) {
        var list=users.findAll().stream().filter(u->status==null||status.equals(u.getStatus()))
                .filter(u->blank(keyword)||contains(u.getNickname(),keyword)||contains(u.getPhone(),keyword)).sorted(Comparator.comparing(com.takeout.domain.AppUser::getId))
                .map(u->new PlatformUserResponse(u.getId(),u.getNickname(),u.getPhone(),u.getStatus(),u.getCreatedAt())).toList(); return page(list,page,size);
    }
    @Transactional public PlatformUserResponse updateUserStatus(Long id,Integer status) {
        var u=users.findById(id).orElseThrow(()->new BusinessException(ErrorCode.NOT_FOUND,"用户不存在")); u.setStatus(status); users.save(u); audit("UPDATE_STATUS","USER",id,"status="+status);
        return new PlatformUserResponse(u.getId(),u.getNickname(),u.getPhone(),u.getStatus(),u.getCreatedAt());
    }
    public PageResult<PlatformShopResponse> listShops(String keyword,Integer status,long page,long size) {
        var list=shops.findAll().stream().filter(s->s.getIsDeleted()==0).filter(s->status==null||status.equals(s.getStatus()))
                .filter(s->blank(keyword)||contains(s.getName(),keyword)||contains(s.getPhone(),keyword)).sorted(Comparator.comparing(com.takeout.domain.Shop::getId))
                .map(this::shop).toList(); return page(list,page,size);
    }
    @Transactional public PlatformShopResponse updateShopStatus(Long id,Integer status) {
        var s=shops.findById(id).filter(x->x.getIsDeleted()==0).orElseThrow(()->new BusinessException(ErrorCode.NOT_FOUND,"商家不存在")); s.setStatus(status); shops.save(s); audit("UPDATE_STATUS","SHOP",id,"status="+status); return shop(s);
    }
    public PageResult<PlatformOrderResponse> listOrders(String orderNo,Long shopId,Integer status,long page,long size) {
        var list=orders.findByIsDeletedOrderByCreatedAtDesc(0).stream().filter(o->shopId==null||shopId.equals(o.getShopId()))
                .filter(o->status==null||status.equals(o.getOrderStatus())).filter(o->blank(orderNo)||contains(o.getOrderNo(),orderNo))
                .map(o->new PlatformOrderResponse(o.getId(),o.getOrderNo(),o.getUserId(),o.getShopId(),shops.findById(o.getShopId()).map(com.takeout.domain.Shop::getName).orElse(""),
                        o.getPayAmount(),o.getPayStatus(),o.getOrderStatus(),o.getCreatedAt())).toList(); return page(list,page,size);
    }
    @Transactional public PlatformShopResponse saveShop(Long id,PlatformShopUpsertRequest r){
      boolean create=id==null;LocalDateTime now=LocalDateTime.now();var s=create?new com.takeout.domain.Shop():shops.findById(id).filter(x->x.getIsDeleted()==0).orElseThrow(()->new BusinessException(ErrorCode.NOT_FOUND,"商家不存在"));
      if(create){if(r.adminUsername()==null||r.adminUsername().isBlank()||r.adminPassword()==null||r.adminPassword().isBlank())throw new BusinessException(ErrorCode.PARAM_ERROR,"新增商家必须填写管理员账号和密码");if(admins.findByUsernameAndStatus(r.adminUsername().trim(),1).isPresent())throw new BusinessException(ErrorCode.CONFLICT,"管理员账号已存在");s.setId(shops.findAll().stream().mapToLong(com.takeout.domain.Shop::getId).max().orElse(0)+1);s.setCreatedAt(now);s.setLogoUrl("");s.setNotice("");s.setIsDeleted(0);}
      s.setName(r.name().trim());s.setPhone(r.phone());s.setAddress(r.address());s.setMinOrderAmount(r.minOrderAmount());s.setDeliveryFee(r.deliveryFee());s.setBusinessStatus(r.businessStatus());s.setStatus(r.status());s.setUpdatedAt(now);shops.save(s);
      if(create){var a=new com.takeout.domain.AdminAccount();a.setId(admins.findAll().stream().mapToLong(com.takeout.domain.AdminAccount::getId).max().orElse(0)+1);a.setUsername(r.adminUsername().trim());a.setPasswordHash(passwordEncoder.encode(r.adminPassword()));a.setDisplayName(s.getName()+"管理员");a.setRole("MERCHANT_ADMIN");a.setShopId(s.getId());a.setShopName(s.getName());a.setStatus(1);a.setCreatedAt(now);a.setUpdatedAt(now);admins.save(a);}audit(create?"CREATE":"UPDATE","SHOP",s.getId(),s.getName());return shop(s);
    }
    public PlatformOrderDetailResponse orderDetail(Long id){var o=orders.findById(id).filter(x->x.getIsDeleted()==0).orElseThrow(()->new BusinessException(ErrorCode.NOT_FOUND,"订单不存在"));var items=orderItems.findByOrderId(id).stream().map(i->new com.takeout.dto.app.OrderItemResponse(i.getDishId(),i.getDishName(),i.getDishImageUrl(),i.getDishPrice(),i.getQuantity(),i.getSizeOption(),i.getSpiceOption(),i.getNotes(),i.getSubtotalAmount())).toList();var refund=refunds.findByOrderId(id).map(r->new com.takeout.dto.app.RefundResponse(r.getId(),r.getRefundNo(),r.getAmount(),r.getStatus(),r.getReason(),r.getSource(),r.getCreatedAt())).orElse(null);return new PlatformOrderDetailResponse(o.getId(),o.getOrderNo(),o.getUserId(),o.getShopId(),shops.findById(o.getShopId()).map(com.takeout.domain.Shop::getName).orElse(""),o.getReceiverName(),o.getReceiverPhone(),o.getReceiverAddress(),o.getRemark(),o.getPayAmount(),o.getPayStatus(),o.getOrderStatus(),items,o.getCreatedAt(),refund);}
    public List<PlatformConfigResponse> listConfigs(){ensureDefaultConfig();return configs.findAll().stream().map(c->new PlatformConfigResponse(c.getConfigKey(),c.getConfigValue(),c.getDescription(),c.getUpdatedAt())).toList();}
    @Transactional public PlatformConfigResponse updateConfig(String key,String value){if(!"order.unpaid_timeout_minutes".equals(key))throw new BusinessException(ErrorCode.PARAM_ERROR,"不支持的配置项");try{int n=Integer.parseInt(value);if(n<1||n>1440)throw new NumberFormatException();}catch(NumberFormatException e){throw new BusinessException(ErrorCode.PARAM_ERROR,"超时时间必须为 1 到 1440 分钟");}var c=configs.findById(key).orElseGet(()->config(key,"15","未支付订单超时分钟数"));c.setConfigValue(value);c.setUpdatedAt(LocalDateTime.now());configs.save(c);audit("UPDATE","CONFIG",key,"value="+value);return new PlatformConfigResponse(c.getConfigKey(),c.getConfigValue(),c.getDescription(),c.getUpdatedAt());}
    public PageResult<PlatformAuditResponse> listAudits(long page,long size){var list=audits.findAllByOrderByCreatedAtDesc().stream().map(a->new PlatformAuditResponse(a.getId(),a.getAdminId(),a.getRole(),a.getAction(),a.getTargetType(),a.getTargetId(),a.getDetail(),a.getCreatedAt())).toList();return page(list,page,size);}
    public PageResult<PlatformMerchantAdminResponse> listMerchantAdmins(Long shopId,long page,long size){var list=admins.findByRoleOrderByIdAsc("MERCHANT_ADMIN").stream().filter(a->shopId==null||shopId.equals(a.getShopId())).map(this::admin).toList();return page(list,page,size);}
    @Transactional public PlatformMerchantAdminResponse updateMerchantAdminStatus(Long id,Integer status){var a=merchantAdmin(id);a.setStatus(status);a.setUpdatedAt(LocalDateTime.now());admins.save(a);audit("UPDATE_STATUS","MERCHANT_ADMIN",id,"status="+status);return admin(a);}
    @Transactional public PlatformMerchantAdminResponse resetMerchantAdminPassword(Long id,String password){var a=merchantAdmin(id);a.setPasswordHash(passwordEncoder.encode(password));a.setFailedLoginAttempts(0);a.setLockedUntil(null);a.setUpdatedAt(LocalDateTime.now());admins.save(a);audit("RESET_PASSWORD","MERCHANT_ADMIN",id,"password changed and login lock cleared");return admin(a);}
    @Transactional public PlatformOrderDetailResponse cancelOrder(Long id,String reason){var o=orders.findLockedById(id).filter(x->x.getIsDeleted()==0).orElseThrow(()->new BusinessException(ErrorCode.NOT_FOUND,"订单不存在"));if(o.getOrderStatus().equals(com.takeout.domain.enums.OrderStatus.REFUNDED.getCode()))return orderDetail(id);if(!o.getOrderStatus().equals(com.takeout.domain.enums.OrderStatus.UNPAID.getCode())&&!o.getOrderStatus().equals(com.takeout.domain.enums.OrderStatus.PAID_WAIT_ACCEPT.getCode()))throw new BusinessException(ErrorCode.CONFLICT,"仅待支付或已支付待接单订单可由平台取消");o.setRemark(blank(o.getRemark())?"平台取消："+reason:o.getRemark()+"；平台取消："+reason);o.setUpdatedAt(LocalDateTime.now());orders.save(o);if(o.getPayStatus().equals(PayStatus.PAID.getCode())){refundService.refund(o.getId(),"platform-cancel-"+o.getId(),reason,"PLATFORM");audit("REFUND","ORDER",id,reason);}else{inventoryService.restore(o.getId());o.setOrderStatus(com.takeout.domain.enums.OrderStatus.CANCELLED.getCode());orders.save(o);audit("CANCEL","ORDER",id,reason);}return orderDetail(id);}
    public PageResult<PlatformAuditResponse> listAudits(Long adminId,String action,String targetType,long page,long size){var list=audits.findAllByOrderByCreatedAtDesc().stream().filter(a->adminId==null||adminId.equals(a.getAdminId())).filter(a->blank(action)||action.equals(a.getAction())).filter(a->blank(targetType)||targetType.equals(a.getTargetType())).map(a->new PlatformAuditResponse(a.getId(),a.getAdminId(),a.getRole(),a.getAction(),a.getTargetType(),a.getTargetId(),a.getDetail(),a.getCreatedAt())).toList();return page(list,page,size);}
    private PlatformShopResponse shop(com.takeout.domain.Shop s){return new PlatformShopResponse(s.getId(),s.getName(),s.getPhone(),s.getAddress(),s.getMinOrderAmount(),s.getDeliveryFee(),s.getBusinessStatus(),s.getStatus());}
    private PlatformMerchantAdminResponse admin(com.takeout.domain.AdminAccount a){return new PlatformMerchantAdminResponse(a.getId(),a.getUsername(),a.getDisplayName(),a.getShopId(),a.getShopName(),a.getStatus());}
    private com.takeout.domain.AdminAccount merchantAdmin(Long id){return admins.findById(id).filter(a->"MERCHANT_ADMIN".equals(a.getRole())).orElseThrow(()->new BusinessException(ErrorCode.NOT_FOUND,"商家管理员不存在"));}
    private void audit(String action,String type,Object id,String detail){var i=adminContext.requireIdentity();var a=new com.takeout.domain.AuditLog();a.setAdminId(i.adminId());a.setRole(i.role());a.setAction(action);a.setTargetType(type);a.setTargetId(String.valueOf(id));a.setDetail(detail);a.setCreatedAt(LocalDateTime.now());audits.save(a);}
    private void ensureDefaultConfig(){if(!configs.existsById("order.unpaid_timeout_minutes"))configs.save(config("order.unpaid_timeout_minutes","15","未支付订单超时分钟数"));}
    private com.takeout.domain.SystemConfig config(String key,String value,String description){var c=new com.takeout.domain.SystemConfig();c.setConfigKey(key);c.setConfigValue(value);c.setDescription(description);c.setUpdatedAt(LocalDateTime.now());return c;}
    private boolean blank(String v){return v==null||v.isBlank();} private boolean contains(String v,String q){return v!=null&&v.toLowerCase().contains(q.trim().toLowerCase());}
    private <T> PageResult<T> page(List<T> all,long page,long size){long p=Math.max(page,1),s=Math.max(size,1);int from=(int)Math.min((p-1)*s,all.size()),to=(int)Math.min(from+s,all.size());return new PageResult<>(all.subList(from,to),all.size(),p,s);}
}
