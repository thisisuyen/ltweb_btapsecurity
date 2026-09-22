package vn.iotstar.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPasswordDTO {

  @Email
  @NotBlank
  private String email;

  @NotBlank
  @Size(min = 4, max = 10)
  private String code;

  @NotBlank
  @Size(min = 6, max = 100)
  private String newPassword;

  @NotBlank
  @Size(min = 6, max = 100)
  private String confirmPassword;

  public ResetPasswordDTO() {}

  public String getEmail() { return email; }
  public void setEmail(String email) { this.email = email; }

  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }

  public String getNewPassword() { return newPassword; }
  public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

  public String getConfirmPassword() { return confirmPassword; }
  public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}