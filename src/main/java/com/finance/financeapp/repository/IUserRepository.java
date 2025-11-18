package com.finance.financeapp.repository;

import com.finance.financeapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad User.
 * Gestiona todas las operaciones de base de datos para los usuarios.
 */
@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

    /**
     * Busca un usuario por su nombre de usuario (que será el email).
     * Spring Data JPA implementará este método automáticamente.
     *
     * @param username El nombre de usuario (email) a buscar.
     * @return un Optional que contiene al usuario si se encuentra, o vacío si no.
     */
    Optional<User> findByUsername(String username);

    /**
     * Verifica si ya existe un usuario con el email dado.
     * Es más eficiente que buscar el objeto completo solo para ver si existe.
     *
     * @param email El email a verificar.
     * @return true si existe, false en caso contrario.
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si ya existe un usuario con el username dado.
     * (Aunque en nuestra lógica email y username son lo mismo,
     * mantener esta semántica separada es una buena práctica).
     *
     * @param username El username a verificar.
     * @return true si existe, false en caso contrario.
     */
    boolean existsByUsername(String username);

}