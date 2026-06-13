package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_room_material")
public class RoomMaterial {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String roomId;

    private Long trainingId;

    private Long uploaderId;

    private String fileName;

    private String fileUrl;

    private Long fileSize;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
