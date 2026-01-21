package pl.mlodyg.spotifer.dto;

import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MeDto {
    @Id
    private String id;

    @NotBlank
    private String spotifyUserId;
    private String displayName;
    private String avatarUrl;
    private Object roles;
}
