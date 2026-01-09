//src/main/java/com/library/dto/RoleDTO.java
package com.library.dto;

import lombok.Data;

@Data
public class RoleDTO {
    private Long id;
    private String name;
    private String description;
    private boolean systemRole;
}