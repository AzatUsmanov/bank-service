package com.example.demo.domain.dto.operation;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.Date;

/**
 * Родительский класс для всех операций
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Operation {

    @Schema(description = "идентификатор", example = "312")
    private Integer id;

    @Schema(description = "Дата создания операции", example = "2011-11-11")
    @NotNull(message = "empty date of creation")
    private Date dateOfCreation;

}
