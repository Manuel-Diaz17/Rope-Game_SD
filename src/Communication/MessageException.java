package Communication;

/**
 *   This data type defines an exception that is thrown if the message is invalid.
 */

public class MessageException extends Exception
{
  /**
   *  Message giving rise to the exception
   */

   private final Message msg;

  /**
   *  Instatiation of a message
   *
   *    @param errorMessage text signalling the error condition
   *    @param msg message behind the exception
   */

   public MessageException (String errorMessage, Message msg)
   {
     super (errorMessage);
     this.msg = msg;
   }

  /**
   *  Obtaining the message that caused the exception.
   *
   *    @return message
   */

   public Message getMessageVal ()
   {
     return (msg);
   }
}
