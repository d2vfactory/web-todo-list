package com.d2vfactory.webtodolist.model.dto;

import lombok.Data;

@Data
public class PagedResourceDTO extends ErrorDTO {

    private TodoListDTO _embedded;

    private PageDTO page;

}
