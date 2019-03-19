package com.d2vfactory.webtodolist.service;

import com.d2vfactory.webtodolist.config.RestTemplateResponseErrorHandler;
import com.d2vfactory.webtodolist.model.dto.PagedResourceDTO;
import com.d2vfactory.webtodolist.model.dto.TodoDTO;
import com.d2vfactory.webtodolist.model.dto.TodoListDTO;
import com.d2vfactory.webtodolist.model.form.TodoForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TodoService {

    private RestTemplate restTemplate;

    @Autowired
    public TodoService(RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder
                .errorHandler(new RestTemplateResponseErrorHandler())
                .build();
    }

    private String host = "127.0.0.1";
    private String port = "8081";

    public Page<TodoDTO> getPageTodoList(Pageable pageable) {

        URI uri = createURIBuilder("/api/todo")
                .queryParam("page", pageable.getPageNumber())
                .queryParam("size", pageable.getPageSize())
                .build()
                .toUri();

        ResponseEntity<PagedResourceDTO> responseEntity = restTemplate.getForEntity(uri.toString(), PagedResourceDTO.class);

        PagedResourceDTO pagedResource = responseEntity.getBody();

        return new PageImpl<>(pagedResource.get_embedded().getTodoList(),
                PageRequest.of(pagedResource.getPage().getNumber(), pagedResource.getPage().getSize()),
                pagedResource.getPage().getTotalElements());
    }

    public List<TodoDTO> getActiveTodoList(List<Long> exceptIds) {
        URI uri = createURIBuilder("/api/todo/allActiveTodo")
                .build()
                .toUri();

        ResponseEntity<TodoListDTO> responseEntity = restTemplate.getForEntity(uri.toString(), TodoListDTO.class);

        List<TodoDTO> newTodoList = responseEntity.getBody().getTodoList();

        if (exceptIds.isEmpty())
            return newTodoList;

        return newTodoList
                .stream().filter(x -> !exceptIds.contains(x.getId()))
                .collect(Collectors.toList());
    }

    public TodoDTO getTodo(Long id) {
        URI uri = createURIBuilder("/api/todo/{id}")
                .buildAndExpand(id)
                .toUri();

        ResponseEntity<TodoDTO> responseEntity = restTemplate.getForEntity(uri.toString(), TodoDTO.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK)
            throw new RuntimeException(responseEntity.getBody().getMessage());

        return responseEntity.getBody();
    }

    public TodoDTO createTodo(String content, Long[] referenceIds) {
        URI uri = createURIBuilder("/api/todo")
                .build()
                .toUri();

        TodoForm todoForm = TodoForm.builder()
                .content(content)
                .referenceIds(referenceIds)
                .build();

        ResponseEntity<TodoDTO> responseEntity = executeCommand(uri.toString(), todoForm, HttpMethod.POST);

        if (responseEntity.getStatusCode() != HttpStatus.CREATED)
            throw new RuntimeException(responseEntity.getBody().getMessage());

        return responseEntity.getBody();
    }

    public TodoDTO updateContent(Long id, String content) {
        URI uri = createURIBuilder("/api/todo/{id}")
                .buildAndExpand(id)
                .toUri();

        TodoForm todoForm = TodoForm.builder().content(content).build();

        ResponseEntity<TodoDTO> responseEntity = executeCommand(uri.toString(), todoForm, HttpMethod.PUT);

        if (responseEntity.getStatusCode() != HttpStatus.OK)
            throw new RuntimeException(responseEntity.getBody().getMessage());

        return responseEntity.getBody();
    }

    public TodoDTO updateStatus(Long id, String status) {
        URI uri = createURIBuilder("/api/todo/{id}/status")
                .buildAndExpand(id)
                .toUri();

        TodoForm todoForm = TodoForm.builder().status(status).build();

        ResponseEntity<TodoDTO> responseEntity = executeCommand(uri.toString(), todoForm, HttpMethod.PUT);
        log.info("# responseEntity : {}", responseEntity);

        if (responseEntity.getStatusCode() != HttpStatus.OK)
            throw new RuntimeException(responseEntity.getBody().getMessage());

        return responseEntity.getBody();
    }

    public TodoDTO addReference(Long id, Long[] referenceIds) {

        URI uri = createURIBuilder("/api/todo/{id}/reference")
                .buildAndExpand(id)
                .toUri();

        TodoForm form = TodoForm.builder()
                .referenceIds(referenceIds)
                .build();

        ResponseEntity<TodoDTO> responseEntity = executeCommand(uri.toString(), form, HttpMethod.PUT);

        if (responseEntity.getStatusCode() != HttpStatus.OK)
            throw new RuntimeException(responseEntity.getBody().getMessage());

        return responseEntity.getBody();
    }

    public TodoDTO removeReference(Long id, Long[] referenceIds) {

        URI uri = createURIBuilder("/api/todo/{id}/reference")
                .buildAndExpand(id)
                .toUri();

        TodoForm form = TodoForm.builder()
                .referenceIds(referenceIds)
                .build();

        ResponseEntity<TodoDTO> responseEntity = executeCommand(uri.toString(), form, HttpMethod.DELETE);

        if (responseEntity.getStatusCode() != HttpStatus.OK)
            throw new RuntimeException(responseEntity.getBody().getMessage());

        return responseEntity.getBody();
    }

    private UriComponentsBuilder createURIBuilder(String path) {
        return UriComponentsBuilder.newInstance()
                .scheme("http").host(host).port(port)
                .path(path);

    }

    public <T> ResponseEntity<TodoDTO> executeCommand(String uri, T form, HttpMethod method) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<T> request = new HttpEntity<>(form, headers);

        return restTemplate.exchange(uri, method, request, TodoDTO.class);
    }
}

