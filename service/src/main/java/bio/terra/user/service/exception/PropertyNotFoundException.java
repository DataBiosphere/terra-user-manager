package bio.terra.user.service.exception;

import bio.terra.common.exception.NotFoundException;

public class PropertyNotFoundException extends NotFoundException {
  public PropertyNotFoundException(String message) {
    super(message);
  }

  public PropertyNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
