package com.d2vfactory.webtodolist.service;

import com.d2vfactory.webtodolist.config.RestTemplateResponseErrorHandler;
import com.d2vfactory.webtodolist.model.dto.TodoDTO;
import com.d2vfactory.webtodolist.model.dto.TodoListDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class TodoServiceTest {

    @Autowired
    private TodoService service;

    @Autowired
    private RestTemplateBuilder builder;

    @Value("${rest-todo-list.host}")
    private String host;

    @Value("${rest-todo-list.port}")
    private String port;

    private RestTemplate restTemplate;

    @Before
    public void setup() {
        restTemplate = builder
                .errorHandler(new RestTemplateResponseErrorHandler())
                .build();

    }

    @Test
    public void getTodoList() {
        Page<TodoDTO> page = service.getPageTodoList(PageRequest.of(0, 10));

        assertThat(page.getContent().size() > 0).isTrue();
        assertThat(page.getTotalElements() > 0).isTrue();
        log.info("todoList : {}", page.getContent());
        log.info("total elements : {}", page.getTotalElements());

    }

    @Test
    public void getTodo1_success() {
        TodoDTO todo = service.getTodo(1L);

        assertThat(todo)
                .hasFieldOrPropertyWithValue("id", 1L);

        log.info("# todo :{}", todo);
    }

    @Test(expected = RuntimeException.class)
    public void getTodo_error() {
        Long maxValue = Long.MAX_VALUE;
        TodoDTO todo = service.getTodo(maxValue);

        log.info("# todo :{}", todo);
    }

    @Test
    public void putTodo() {
        Long id = 2L;
        String content = "할일 수정";

        TodoDTO todo = service.updateContent(id, content);

        assertThat(todo)
                .hasFieldOrPropertyWithValue("id", 2L)
                .hasFieldOrPropertyWithValue("content", "할일 수정");

        todo = service.updateContent(id, "빨래");

        assertThat(todo)
                .hasFieldOrPropertyWithValue("id", 2L)
                .hasFieldOrPropertyWithValue("content", "빨래");


    }

    @Test
    public void putTodoStatus() {
        Long id = 2L;

        String status = "COMPLETED";

        TodoDTO todo = service.updateStatus(id, status);
        assertThat(todo)
                .hasFieldOrPropertyWithValue("status","COMPLETED");

        todo = service.updateStatus(id, "ACTIVE");
        assertThat(todo)
                .hasFieldOrPropertyWithValue("status","ACTIVE");

        log.info("todo : {}", todo);

    }

    @Test
    public void getAllActiveTodo() {
        URI uri = createURIBuilder("/api/todo/allActiveTodo")
                .build()
                .toUri();

        ResponseEntity<TodoListDTO> entity = restTemplate.getForEntity(uri.toString(), TodoListDTO.class);

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody().getTodoList().size() > 0).isTrue();

        log.info("entity : {}", entity.getBody());
    }

    @Test
    public void todo2_addReference_todo1to3_remove() {
        Long id = 2L;
        Long[] referenceIds = {1L, 2L, 3L};
        // 2는 제외하고 1,3이 추가됨
        TodoDTO todo = service.addReference(id, referenceIds);

        assertThat(todo.getReference())
                .extracting("id")
                .containsExactly(1L, 3L);

        todo = service.removeReference(id, referenceIds);

        if (todo.getReference().size() > 0) {
            assertThat(todo.getReference())
                    .extracting("id")
                    .doesNotContain(1L, 2L, 3L);
        }
    }

    @Test
    public void createTodo_reference() {
        String content = "숙제하기";
        Long[] referenceIds = {1L};

        TodoDTO todo = service.createTodo(content, referenceIds);

        assertThat(todo.getId()).isNotNull();

        log.info("# todo :{}", todo);

    }

    @Test
    public void createTodo_noReference() {
        String content = "집에가기";

        TodoDTO todo = service.createTodo(content, new Long[] {});

        assertThat(todo.getId()).isNotNull();

        log.info("# todo :{}", todo);

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