package client;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class Client {
	private final Socket socket;
	private final DataInputStream in;
	private final DataOutputStream out;
	private String[] elements;

	public Client() throws IOException {
		socket = new Socket("localhost", 1235);
		in = new DataInputStream(socket.getInputStream());
		out = new DataOutputStream(socket.getOutputStream());
		elements = getFilesList();
		runClient();
	}

	private void runClient() {
		JFrame frame = new JFrame("Cloud Storage");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(600, 500);
		JTextArea ta = new JTextArea();
 		JList list = new JList(elements);
 		JButton uploadButton = new JButton("Upload");
		JButton downloadButton = new JButton("Download");
		JButton removeButton = new JButton("Remove");

		Container container = frame.getContentPane();
		container.setLayout(new GridLayout(5, 1 , 2,2 ));
		container.add(ta);
		container.add(list);
		container.add(uploadButton);
		container.add(downloadButton);
		container.add(removeButton);
		frame.setVisible(true);

		list.addListSelectionListener(evt -> {
			if (!evt.getValueIsAdjusting()) {
				String val = "";
				if(list.getSelectedValue() != null){
					val = list.getSelectedValue().toString();
				}
				ta.setText(val);
			}
		});

		uploadButton.addActionListener(a -> {
			System.out.println(sendFile(ta.getText()));
			elements = getFilesList();
			list.setListData(elements);
		});

		downloadButton.addActionListener(a -> {
			System.out.println(getFile(ta.getText()));
			elements = getFilesList();
			list.setListData(elements);
		});

		removeButton.addActionListener(a->{
			System.out.println(removeFile(ta.getText()));
			elements = getFilesList();
			list.setListData(elements);
		});

	}

//	private JList refreshFilesList () {
//		File dir = new File("client");
//		File[] arrFiles = dir.listFiles();
//		ArrayList<String> elements = new ArrayList<>();
//		for (File arrFile : arrFiles) {
//			if (arrFile.isFile()) elements.add(arrFile.getName());
//		}
//		 return new JList(elements.toArray(new String[elements.size()]));
//	}

	private String removeFile(String filename) {
		try {
			out.writeUTF("remove");
			out.writeUTF(filename);
			out.flush();
			return in.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	private String getFile(String filename) {
		String command;
		try {
			out.writeUTF("download");
			out.writeUTF(filename);
			out.flush();
			command = in.readUTF();
			if(command.equals("success")) {

				File file = new File("client" + File.separator + in.readUTF());
				if (!file.exists()) file.createNewFile();
				long size = in.readLong();
				FileOutputStream fos = new FileOutputStream(file);
				byte[] buffer = new byte[256];
				for (int i = 0; i < (size + 255) / 256; i++) {
					int read = in.read(buffer);
					fos.write(buffer, 0, read);
				}
				fos.close();
				out.writeUTF("DOWNLOAD FILE SUCCESSFUL");
				return command;
			}
			return "File Not Found";

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	private String sendFile(String filename) {
		try {
			File file = new File("client" + File.separator + filename);
			if (file.exists()) {
				out.writeUTF("upload");
				out.writeUTF(filename);
				long length = file.length();
				out.writeLong(length);
				FileInputStream fis = new FileInputStream(file);
				int read;
				byte[] buffer = new byte[256];
				while ((read = fis.read(buffer)) != -1) {
					out.write(buffer, 0, read);
				}
				out.flush();
				fis.close();
				return  in.readUTF();
			} else {
				return "File is not exists";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Something error";
	}

	private String[] getFilesList(){
		String str = "null";
		try{
			out.writeUTF("files");
			str = in.readUTF();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str.trim().split("\\*");
	}

	public static void main(String[] args) throws IOException {
		new Client();
	}
}
