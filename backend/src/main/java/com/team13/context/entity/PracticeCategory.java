package com.team13.context.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ctx_practice_category")
public class PracticeCategory {

    @TableId
    private String id;

    private String title;

    private String subtitle;

    private String icon;

    private String gradientStart;

    private String gradientEnd;

    private Integer todayCount;

    private String span;

    private Integer sortOrder;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
