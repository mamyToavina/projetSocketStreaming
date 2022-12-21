/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlMedia;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.Player;


public final class MediaClient extends Application{
    private Player jlPlayer;
    private final Socket k;
    private ImageIcon img;
    private BufferedImage bufferedImage;
    private Media video;
    private MediaPlayer mediaPlayer;

    public MediaClient() throws IOException, InterruptedException {
        this.k=new Socket("localhost",9080);  
    }
    
    public byte[] streamToBytes(InputStream inputStream) {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try {
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
        }
        byte[] bytes = output.toByteArray();
        return bytes;
    }
    

    public void playMusic() throws IOException, InterruptedException {
        byte[] data=new byte[4096];
        new Thread() {
            @Override
            public void run() {
                try {
                    DataInputStream entree=new DataInputStream(k.getInputStream());
                    InputStream mus;
                    while(entree.read()!=-1){
                        entree.read(data);
                        mus=new DataInputStream(new ByteArrayInputStream(data));
                        jlPlayer = new Player(mus);
                        
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    //Thread.sleep(700);
                                    jlPlayer.play();
                                    
                                } catch (JavaLayerException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }.start();
                        
                    }
                } catch (IOException | JavaLayerException  ex) {
                    
                }
            }
        }.start();

    }
    
    
    public void showImage() throws IOException{
        JFrame jFrame=new JFrame("image recu");
        jFrame.setSize(400,400);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JLabel jLabelText=new JLabel("waiting for image from client...");
        jFrame.add(jLabelText,BorderLayout.SOUTH);
        jFrame.setVisible(true);
        
        
        BufferedInputStream bufferedInputStream;
        InputStream inputStream = k.getInputStream();
            bufferedInputStream = new BufferedInputStream(inputStream);
        
        
        new Thread(){
            
            @Override
            public void run(){
                
                try {
                    bufferedImage = ImageIO.read(bufferedInputStream);
                } catch (IOException ex) {
                    Logger.getLogger(MediaClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                JLabel jlabelPic=new JLabel(new ImageIcon(bufferedImage));
                jLabelText.setText("image received");
                jFrame.add(jlabelPic,BorderLayout.CENTER);
            }
        }.start();
    }
    
    public void sendChoiceToServer() throws IOException{
        System.out.println("\nyour choice: ");
        Scanner sc=new Scanner(System.in);
        int num=sc.nextInt();
        if(num==0 && jlPlayer!=null){
            jlPlayer.close();
        }
        else{
            DataOutputStream out=new DataOutputStream(k.getOutputStream());
            out.writeInt(num);
            out.flush();
            System.out.println("wait for a second...");
        }
    }
    
        
    public void sendTypeToServer() throws IOException{
        System.out.println("type :1:MUSIC\n       2:IMAGES\n       3:VIDEOS\n       0:QUIT\n");
        Scanner sc=new Scanner(System.in);
        int num=sc.nextInt();
        DataOutputStream out=new DataOutputStream(k.getOutputStream());
        out.writeInt(num);
        out.flush();
        System.out.println("wait for a second...");
    }
    
    public void showList() throws IOException{
        DataInputStream listToShow=new DataInputStream(k.getInputStream());
        String list=listToShow.readUTF();
        System.out.println(list);
    }
    
    public void startFile() throws IOException, InterruptedException{
        DataInputStream listMusic=new DataInputStream(k.getInputStream());
        String list=listMusic.readUTF();
        switch (list) {
            case "send music..." -> this.playMusic();
            case "send photo..." -> this.showImage();
            case "send video..." -> Application.launch();
            default -> this.sendTypeToServer();
        }
    }
    
    
    @Override
    public void start(Stage primaryStage) {
        
        byte[] data=new byte[4096];
        new Thread(){
            File tempMp3;
            Media media;
            FileOutputStream fos;
            DataInputStream entree;
            MediaView viewer;
            DoubleProperty width;
            DoubleProperty height;
            StackPane root;
            Scene scene ;
            @Override
            public void run(){
            
                
                try {
                    entree = new DataInputStream(k.getInputStream());
                } catch (IOException ex) {
                    Logger.getLogger(MediaClient.class.getName()).log(Level.SEVERE, null, ex);
                }
                InputStream mus;
                byte[] tuneAsBytes;
                try {
                    while(entree.read()!=-1){
                        try {
                            entree.read(data);
                            mus=new DataInputStream(new ByteArrayInputStream(data));
                            tuneAsBytes= new MediaClient().streamToBytes(mus);
                            tempMp3 = File.createTempFile("temp", ".mp4", null); //, getCacheDir()
                            tempMp3.deleteOnExit();
                            fos = new FileOutputStream(tempMp3);
                            fos.write(tuneAsBytes);
                            System.out.println(tempMp3.getAbsolutePath());
                            media = new Media(tempMp3.toURI().toURL().toString());
                            mediaPlayer = new MediaPlayer(media);
                            viewer = new MediaView(mediaPlayer);

                            // Ajuster automatiquement la vue vidéo à la taille de la scène :
                            width = viewer.fitWidthProperty();
                            height = viewer.fitHeightProperty();
                            width.bind(Bindings.selectDouble(viewer.sceneProperty(), "width"));
                            height.bind(Bindings.selectDouble(viewer.sceneProperty(), "height"));
                            viewer.setPreserveRatio(true);

                            root = new StackPane();
                            root.getChildren().add(viewer);

                            scene = new Scene(root, 500, 500);

                            primaryStage.setScene(scene);
                            primaryStage.setTitle("Vidéo");
                            primaryStage.setFullScreen(false);
                            primaryStage.show();
                            mediaPlayer.play();
                            
                            
                        } catch (IOException | InterruptedException e) {
                        }
                            
                    }
                } catch (IOException ex) {
                    Logger.getLogger(MediaClient.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }.start();

    }

    
    public static void main(String args[]) throws IOException, InterruptedException{
        MediaClient mClient;
            mClient=new MediaClient();
            mClient.sendTypeToServer();
            mClient.showList();
            mClient.sendChoiceToServer();
            mClient.startFile();
    }
    
    
}
