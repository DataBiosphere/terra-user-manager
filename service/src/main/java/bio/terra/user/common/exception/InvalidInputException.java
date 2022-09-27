package bio.terra.user.common.exception;

import bio.terra.common.exception.BadRequestException;

public class InvalidInputException extends BadRequestException {
  public InvalidInputException(String message) {
    super(message);
  }
}
