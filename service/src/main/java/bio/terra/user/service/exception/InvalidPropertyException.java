package bio.terra.user.service.exception;

import bio.terra.common.exception.BadRequestException;

public class InvalidPropertyException extends BadRequestException {
  public InvalidPropertyException(String message) {
    super(message);
  }

  public InvalidPropertyException(String message, Throwable cause) {
    super(message, cause);
  }
}
