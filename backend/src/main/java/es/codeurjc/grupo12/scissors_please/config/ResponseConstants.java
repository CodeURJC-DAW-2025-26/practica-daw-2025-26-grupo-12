package es.codeurjc.grupo12.scissors_please.config;

public class ResponseConstants {

  public static String IMAGE_ERROR_UPLOAD = "The image wasn't uploaded correctly.";
  public static String TOURNAMENT_NOT_FOUND = "The tournament is no longer in the database.";
  public static String ELEMENT_NOT_FOUND = "The element is no longer in the database.";
  public static String DATE_INVALID = "The date entered is invalid.";
  public static String BOT_NOT_FOUND = "The bot is no longer in the database.";
  public static String ACCESS_DENIED = "You do not have permission to access this resource.";
  public static String OK = "The request has been processed correctly";
  // For the models
  public static final String BAD_REQUEST_CODE = "400";
  public static final String UNAUTHORIZED_CODE = "401";
  public static final String FORBIDDEN_CODE = "403";
  public static final String NOT_FOUND_CODE = "404";
  public static final String CONFLICT_CODE = "409";
  public static final String INTERNAL_SERVER_ERROR_CODE = "500";
  public static final String NOT_IMPLEMENTED_CODE = "501";

  // For de APi Rest
  public static final int BAD_REQUEST_CODE_INT = 400;
  public static final int UNAUTHORIZED_CODE_INT = 401;
  public static final int FORBIDDEN_CODE_INT = 403;
  public static final int NOT_FOUND_CODE_INT = 404;
  public static final int CONFLICT_CODE_INT = 409;
  public static final int INTERNAL_SERVER_ERROR_CODE_INT = 500;
  public static final int NOT_IMPLEMENTED_CODE_INT = 501;
  public static final int OK_CODE_INT = 200;
}
