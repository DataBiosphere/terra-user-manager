package bio.terra.user.db.exception;

import bio.terra.common.exception.BadRequestException;

public class BadPathException extends BadRequestException {
  public BadPathException(String message) {
    super(message);
  }

  public BadPathException(String message, Throwable cause) {
    super(message, cause);
  }
}
