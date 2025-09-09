package com.example.hospitalqueue.web;

import com.example.hospitalqueue.domain.Department;
import com.example.hospitalqueue.service.DepartmentService;
import com.example.hospitalqueue.service.TicketService;
import com.example.hospitalqueue.web.dto.QueueStatusResponse;
import com.example.hospitalqueue.web.dto.DepartmentDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentService departmentService;
    private final TicketService ticketService;

    @GetMapping
    public List<DepartmentDto> all() {
        return departmentService.getAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}/queue")
    public QueueStatusResponse queueStatus(@PathVariable Long id) {
        return ticketService.getQueueStatus(id);
    }

    @GetMapping("/{id}")
    public DepartmentDto get(@PathVariable Long id) {
        return toDto(departmentService.getById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentDto create(@Valid @RequestBody DepartmentDto dto) {
        Department d = new Department();
        BeanUtils.copyProperties(dto, d);
        return toDto(departmentService.create(d));
    }

    @PutMapping("/{id}")
    public DepartmentDto update(@PathVariable Long id, @Valid @RequestBody DepartmentDto dto) {
        Department d = new Department();
        BeanUtils.copyProperties(dto, d);
        return toDto(departmentService.update(id, d));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private DepartmentDto toDto(Department d) {
        DepartmentDto dto = new DepartmentDto();
        BeanUtils.copyProperties(d, dto);
        return dto;
    }
}
