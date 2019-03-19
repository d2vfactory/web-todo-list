package com.d2vfactory.webtodolist.controller;

import com.d2vfactory.webtodolist.model.dto.TodoDTO;
import com.d2vfactory.webtodolist.model.form.TodoForm;
import com.d2vfactory.webtodolist.service.TodoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@RequestMapping("/todo")
@Controller
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @GetMapping
    public String getTodoList(Model model,
                              @RequestParam("page") Optional<Integer> page,
                              @RequestParam("size") Optional<Integer> size) {

        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);

        Pageable pageable = PageRequest.of(currentPage - 1, pageSize);

        Page<TodoDTO> todoPage = service.getPageTodoList(pageable);

        model.addAttribute("todoPage", todoPage);

        int totalPages = todoPage.getTotalPages();
        if (totalPages > 0) {
            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
                    .boxed()
                    .collect(Collectors.toList());
            model.addAttribute("pageNumbers", pageNumbers);
        }

        return "todo/todo-list";
    }

    @GetMapping("/form")
    public String formTodo(Model model) {
        List<Long> exceptIds = new ArrayList<>();
        List<TodoDTO> newTodoList = service.getActiveTodoList(exceptIds);

        model.addAttribute("newTodoList", newTodoList);

        return "todo/todo-form";
    }

    @PostMapping("/create")
    public String createTodo(Model model, @ModelAttribute TodoForm todoForm)  {

        TodoDTO todoDTO = service.createTodo(todoForm.getContent(), todoForm.getReferenceIds());
        
        return "redirect:/todo/"+todoDTO.getId();
    }

    @GetMapping("/{id}")
    public String getTodo(Model model, @PathVariable Long id) {
        TodoDTO todo = service.getTodo(id);

        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("ACTIVE", "진행중");
        statusMap.put("COMPLETED", "완료");


        long cntReferenceActive = todo.getReference()
                .stream().filter(x -> x.getStatus().equals("ACTIVE"))
                .count();

        model.addAttribute("todo", todo);
        model.addAttribute("statusMap", statusMap);
        model.addAttribute("cntReferenceActive", cntReferenceActive);
        return "todo/todo-desc";
    }


    @PostMapping("/{id}")
    public String updateContent(@PathVariable Long id, @ModelAttribute TodoForm todoForm) {
        service.updateContent(id, todoForm.getContent());
        return "redirect:/todo/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id, @ModelAttribute TodoForm todoForm) {
        log.info("# todoForm : {}", todoForm);
        service.updateStatus(id, todoForm.getStatus());
        return "redirect:/todo/" + id;
    }


    @GetMapping("/{id}/reference")
    public String getTodoReference(Model model, @PathVariable Long id) {
        TodoDTO todo = service.getTodo(id);

        List<Long> exceptIds = todo.getReference().stream().map(x -> x.getId()).collect(Collectors.toList());
        exceptIds.add(id);

        List<TodoDTO> newTodoList = service.getActiveTodoList(exceptIds);

        model.addAttribute("todo", todo);
        model.addAttribute("newTodoList", newTodoList);

        return "todo/todo-reference";
    }


    @PostMapping("/{id}/reference/add")
    public String addReference(@PathVariable Long id, @ModelAttribute TodoForm todoForm) {
        service.addReference(id, todoForm.getReferenceIds());
        return "redirect:/todo/" + id + "/reference";
    }

    @PostMapping("/{id}/reference/remove")
    public String removeReference(@PathVariable Long id, @ModelAttribute TodoForm todoForm) {
        service.removeReference(id, todoForm.getReferenceIds());
        return "redirect:/todo/" + id + "/reference";
    }


}
