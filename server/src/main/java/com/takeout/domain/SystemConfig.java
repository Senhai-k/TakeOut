package com.takeout.domain;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="system_config")
public class SystemConfig {
 @Id @Column(length=100) private String configKey;
 @Column(nullable=false,length=500) private String configValue;
 @Column(nullable=false,length=200) private String description;
 @Column(nullable=false) private LocalDateTime updatedAt;
 public String getConfigKey(){return configKey;} public void setConfigKey(String v){configKey=v;}
 public String getConfigValue(){return configValue;} public void setConfigValue(String v){configValue=v;}
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public LocalDateTime getUpdatedAt(){return updatedAt;} public void setUpdatedAt(LocalDateTime v){updatedAt=v;}
}
