package ClientSide;

import java.io.*;
import java.net.*;

/**
Communication manager - client side.
 *
 *   Communication is based on message passing over sockets using the TCP protocol.
 *   It supposes the setup of a communication channel between the two end points before data transfer can take place.
 *   Data transfer is bidirectional and is made through the transmission and the reception of objects in output and
 *   input streams, respectively.
 */

public class ClientCom
{
  /**
   *  Communication socket.
   *    @serialField commSocket
   */

   private Socket commSocket = null;

  /**
   *  Name of the computational system where the server is located.
   *    @serialField serverHostName
   */

   private String serverHostName = null;

  /**
   *  Number of the listening port at the computational system where the server is located.
   *    @serialField serverPortNumb
   */

   private int serverPortNumb;

  /**
   *  Input stream of the communication channel.
   *    @serialField in
   */

   private ObjectInputStream in = null;

  /**
   *  Output stream of the communication channel.
   *    @serialField out
   */

   private ObjectOutputStream out = null;

  /**
   *  Instantiation of a communication channel.
   *
   *    @param hostName name of the computational system where the server is located
   *    @param portNumb number of the listening port at the computational system where the server is located
   */

   public ClientCom (String hostName, int portNumb)
   {
      serverHostName = hostName;
      serverPortNumb = portNumb;
   }

    /**
   *  Open the communication channel.
   *
   *  Instantiation of the communication socket and its binding to the server address.
   *  The socket input and output streams are opened.
   *
   *    @return true, if the communication channel is opened -
   *            false, otherwise
   */

   public boolean open ()
   {
      boolean success = true;
      SocketAddress serverAddress = new InetSocketAddress (serverHostName, serverPortNumb);

      try
      { commSocket = new Socket();
        commSocket.connect (serverAddress);
      }
      catch (UnknownHostException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - o nome do sistema computacional onde reside o servidor é desconhecido: " +
                                 serverHostName + "!");
        e.printStackTrace ();
        System.exit (1);
      }
      catch (NoRouteToHostException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - o nome do sistema computacional onde reside o servidor é inatingível: " +
                                 serverHostName + "!");
        e.printStackTrace ();
        System.exit (1);
      }
      catch (ConnectException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - o servidor não responde em: " + serverHostName + "." + serverPortNumb + "!");
        if (e.getMessage ().equals ("Connection refused"))
           success = false;
           else { System.out.println(e.getMessage () + "!");
                  e.printStackTrace ();
                  System.exit (1);
                }
      }
      catch (SocketTimeoutException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - ocorreu um time out no estabelecimento da ligação a: " +
                                 serverHostName + "." + serverPortNumb + "!");
        success = false;
      }
      catch (IOException e)                           // erro fatal --- outras causas
      { System.out.println(Thread.currentThread ().getName () +
                                 " - ocorreu um erro indeterminado no estabelecimento da ligação a: " +
                                 serverHostName + "." + serverPortNumb + "!");
        e.printStackTrace ();
        System.exit (1);
      }

      if (!success) return (success);

      try
      { out = new ObjectOutputStream (commSocket.getOutputStream ());
      }
      catch (IOException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - não foi possível abrir o canal de saída do socket!");
        e.printStackTrace ();
        System.exit (1);
      }

      try
      { in = new ObjectInputStream (commSocket.getInputStream ());
      }
      catch (IOException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - não foi possível abrir o canal de entrada do socket!");
        e.printStackTrace ();
        System.exit (1);
      }

      return (success);
   }

  /**
   *  Close the communication channel.
   *
   *  The socket input and output streams are closed.
   *  The communication socket is closed.
   */

   public void close ()
   {
      try
      { in.close();
      }
      catch (IOException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - não foi possível fechar o canal de entrada do socket!");
        e.printStackTrace ();
        System.exit (1);
      }

      try
      { out.close();
      }
      catch (IOException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - não foi possível fechar o canal de saída do socket!");
        e.printStackTrace ();
        System.exit (1);
      }

      try
      { commSocket.close();
      }
      catch (IOException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - não foi possível fechar o socket de comunicação!");
        e.printStackTrace ();
        System.exit (1);
      }
   }

  /**
   *  Object read from the communication channel.
   *
   *    @return reference to the object that was read
   */

   public Object readObject ()
   {
      Object fromServer = null;                            // objecto

      try
      { fromServer = in.readObject ();
      }
      catch (InvalidClassException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - o objecto lido não é passível de desserialização!");
        e.printStackTrace ();
        System.exit (1);
      }
      catch (IOException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - erro na leitura de um objecto do canal de entrada do socket de comunicação!");
        e.printStackTrace ();
        System.exit (1);
      }
      catch (ClassNotFoundException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - o objecto lido corresponde a um tipo de dados desconhecido!");
        e.printStackTrace ();
        System.exit (1);
      }

      return fromServer;
   }

  /**
   *  Object write to the communication channel.
   *
   *    @param toServer reference to the object to be written
   */

   public void writeObject (Object toServer)
   {
      try
      { out.writeObject (toServer);
      }
      catch (InvalidClassException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - o objecto a ser escrito não é passível de serialização!");
        e.printStackTrace ();
        System.exit (1);
      }
      catch (NotSerializableException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - o objecto a ser escrito pertence a um tipo de dados não serializável!");
        e.printStackTrace ();
        System.exit (1);
      }
      catch (IOException e)
      { System.out.println(Thread.currentThread ().getName () +
                                 " - erro na escrita de um objecto do canal de saída do socket de comunicação!");
        e.printStackTrace ();
        System.exit (1);
      }
   }
}
