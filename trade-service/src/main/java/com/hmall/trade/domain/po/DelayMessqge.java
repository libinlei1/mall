package com.hmall.trade.domain.po;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DelayMessqge {
    private Long orderId;
    private Map<Long, Integer> itemNumMap;
}
