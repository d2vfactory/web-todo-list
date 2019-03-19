package com.d2vfactory.webtodolist.model.dto;

import lombok.Data;
import org.springframework.hateoas.core.Relation;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TodoDTO extends ErrorDTO{

    private Long id;

    private String content;

    private String contentAndReferenced;

    private String status;

    private String statusName;

    private String createDate;

    private String updateDate;

    private String completeDate;

    private List<ReferenceTodoDTO> reference;

    private List<ReferenceTodoDTO> referenced;

}
