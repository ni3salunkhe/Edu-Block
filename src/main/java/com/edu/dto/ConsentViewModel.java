package com.edu.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConsentViewModel {
    private List<ConsentDTO> pending;
    private List<ConsentDTO> active;
}
