package com.takeout.dto.admin;
import com.takeout.dto.app.OrderItemResponse;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.takeout.dto.app.RefundResponse;
public record PlatformOrderDetailResponse(Long id,String orderNo,Long userId,Long shopId,String shopName,String receiverName,String receiverPhone,
 String receiverAddress,String remark,BigDecimal payAmount,Integer payStatus,Integer orderStatus,List<OrderItemResponse> items,LocalDateTime createdAt,RefundResponse refund){}
