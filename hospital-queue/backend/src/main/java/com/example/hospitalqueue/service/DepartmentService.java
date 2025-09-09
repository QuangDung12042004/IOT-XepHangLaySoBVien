package com.example.hospitalqueue.service;

import com.example.hospitalqueue.domain.Department;
import com.example.hospitalqueue.repository.DepartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public List<Department> getAll() {
        return departmentRepository.findAll();
    }

    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy khoa với id=" + id));
    }

    @Transactional
    public Department create(Department d) {
        if (departmentRepository.existsByCode(d.getCode())) {
            throw new IllegalArgumentException("Mã khoa đã tồn tại");
        }
        return departmentRepository.save(d);
    }

    @Transactional
    public Department update(Long id, Department update) {
        Department d = getById(id);
        if (!d.getCode().equals(update.getCode()) && departmentRepository.existsByCode(update.getCode())) {
            throw new IllegalArgumentException("Mã khoa đã tồn tại");
        }
        d.setCode(update.getCode());
        d.setName(update.getName());
        d.setLocation(update.getLocation());
        return departmentRepository.save(d);
    }

    @Transactional
    public void delete(Long id) {
        departmentRepository.deleteById(id);
    }
}
