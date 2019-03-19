package com.d2vfactory.webtodolist.model.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoForm {

    private String content;

    private String status;

    private Long[] referenceIds;
}
