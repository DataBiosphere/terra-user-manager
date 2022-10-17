package bio.terra.user.testutils;

import java.util.Random;

public class TestUtils {
  private static final Random RANDOM = new Random();

  public static String appendRandomNumber(String string) {
    return string + "-" + RANDOM.nextInt(Integer.MAX_VALUE);
  }
}
