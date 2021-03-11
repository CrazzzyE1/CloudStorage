package server;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;

/**
 * Обработчик входящих клиентов
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class.getName());

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream in = new DataInputStream(socket.getInputStream())) {
            while (true) {
                String command = in.readUTF();
                if ("upload".equals(command)) {
                    try {
                        File file = new File("server" + File.separator + in.readUTF());
                        if (!file.exists()) file.createNewFile();
                        long size = in.readLong();
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] buffer = new byte[256];
                        for (int i = 0; i < (size + 255) / 256; i++) {
                            int read = in.read(buffer);
                            fos.write(buffer, 0, read);
                        }
                        fos.close();
                        LOGGER.info("UPLOAD FILE SUCCESSFUL");
                        out.writeUTF("UPLOAD FILE SUCCESSFUL");
                    } catch (Exception e) {
                        LOGGER.info("UPLOAD FILE ERROR");
                        out.writeUTF("ERROR");
                    }
                } else if ("remove".equals(command)) {
                    File file = new File("server" + File.separator + in.readUTF());
                    if (file.delete()) {
                        LOGGER.info("File removed");
                        out.writeUTF("REMOVE SUCCESSFUL");
                    } else {
                        LOGGER.info("REMOVE FILE ERROR");
                        out.writeUTF("REMOVE FILE ERROR");
                    }

                } else if ("download".equals(command)) {
                    File file = new File("server" + File.separator + in.readUTF());
                    if (file.exists()) {
                        out.writeUTF("success");
                        out.writeUTF(file.getName());
                        long length = file.length();
                        out.writeLong(length);
                        FileInputStream fis = new FileInputStream(file);
                        int read;
                        byte[] buffer = new byte[256];
                        while ((read = fis.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                        }
                        out.flush();
                        LOGGER.info(in.readUTF());
                        fis.close();
                    } else {
                        LOGGER.info("File NOT FOUND");
                    }
                } else if ("files".equals(command)) {
                    out.writeUTF(getFileslist().toString());
                }
            }
        } catch (IOException e) {
            LOGGER.info("Client disconnected");
            //e.printStackTrace();
        }
    }

    private StringBuilder getFileslist() {
        File dir = new File("server");
        File[] arrFiles = dir.listFiles();
        StringBuilder sb = new StringBuilder();
        for (File arrFile : arrFiles) {
            if (arrFile.isFile()) {
                sb.append(arrFile.getName());
                sb.append("*");
            }
        }
        return sb;
    }

}
