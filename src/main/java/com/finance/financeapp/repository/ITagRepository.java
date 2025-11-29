package com.finance.financeapp.repository;

import com.finance.financeapp.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ITagRepository extends JpaRepository<Tag, Long> {
    // Necesitamos buscar varias etiquetas a la vez por sus IDs
    // El "IN" de SQL es muy eficiente aquí.
    List<Tag> findAllByIdIn(Set<Long> ids);

    // Validar propiedad (Seguridad Hard Mode)
    // No queremos que un usuario use etiquetas de otro usuario.
    boolean existsByIdAndUserId(Long id, Long userId);
}