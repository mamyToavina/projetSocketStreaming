/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controlMedia;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;


public final class ServeurMedia {
    private ServerSocket ss;
    private Socket sc;
    private ArrayList<String> listeMusic=new ArrayList<>();
    private ArrayList<String> listeImage=new ArrayList<>();
    private ArrayList<String> listeVideo=new ArrayList<>();
    private int mark;//1 ra music no andrasan client;2 ra sary 3 ra video
    
    public ServeurMedia() throws FileNotFoundException, IOException, InterruptedException{
        ss=new ServerSocket(9080);
        sc=ss.accept();
        
        String liste1="D:/music/07 Ndray andro any.mp3";
        String liste2="D:/music/Gasikara (Zaka Rajaonia).mp3";
        String liste3="D:/music/Fitiavako masiaka - Ny Ainga.mp3";
        String liste4="D:/music/AMBONDRONA - Antso ( CLIP GASY ).mp3";
        
        String im1="D:/image/Acer_Wallpaper_01_5000x2814.jpg";
        String im2="D:/image/Acer_Wallpaper_02_5000x2813.jpg";
        String im3="D:/image/Acer_Wallpaper_03_5000x2814.jpg";
        String im4="D:/image/Planet9_Wallpaper_5000x2813.jpg";
        
        String vid1="D:/image/Acer_Wallpaper_01_5000x2814.jpg";
        String vid2="D:/image/Acer_Wallpaper_02_5000x2813.jpg";
        String vid3="D:/image/Acer_Wallpaper_03_5000x2814.jpg";
        String vid4="D:/tuto vacance/[TUTO-JAVA] #3 Manipuler un fichier TXT en Java.mp4";
        
        listeMusic.add(liste1);
        listeMusic.add(liste2);
        listeMusic.add(liste3);
        listeMusic.add(liste4);
        
        listeImage.add(im1);
        listeImage.add(im2);
        listeImage.add(im3);
        listeImage.add(im4);
        
        listeVideo.add(vid1);
        listeVideo.add(vid2);
        listeVideo.add(vid3);
        listeVideo.add(vid4);
        
    }
    
    public void sendListMusicToClient() throws IOException{
        DataOutputStream listToSend=new DataOutputStream(getSc().getOutputStream());
        String liste="Tapez le numero de la music que vous voulez ecouter : ";
        for(int i=0;i<getListeMusic().size();i++){
            liste+="\n"+(i+1)+" - "+" "+getTitleFile(getListeMusic().get(i));
        }
        
        listToSend.writeUTF(liste);
        listToSend.flush();
    }
    
    public void sendListImageToClient() throws IOException{
        DataOutputStream listToSend=new DataOutputStream(getSc().getOutputStream());
        String liste="Tapez le numero de l image que vous voulez afficher : ";
        for(int i=0;i<getListeImage().size();i++){
            liste+="\n"+(i+1)+" - "+" "+getTitleFile(getListeImage().get(i));
        }
        
        listToSend.writeUTF(liste);
        listToSend.flush();
    }
    
    public void sendListVideoToClient() throws IOException{
        DataOutputStream listToSend=new DataOutputStream(getSc().getOutputStream());
        String liste="video numero?? : ";
        for(int i=0;i<getListeVideo().size();i++){
            liste+="\n"+(i+1)+" - "+" "+getTitleFile(getListeVideo().get(i));
        }
        
        listToSend.writeUTF(liste);
        listToSend.flush();
    }
    
    public void sendMusicToPlay(int num) throws FileNotFoundException, IOException, InterruptedException{
        File f=new File(getListeMusic().get(num-1));
        DataInputStream dis;
        try (FileInputStream fis = new FileInputStream(f)) {
            dis = new DataInputStream(fis);
            byte[] data=new byte[4096];
            int buffer=0;
            int transfere=0;
            int pourcentage=0;
            OutputStream dout=new DataOutputStream(getSc().getOutputStream());
            while((buffer=dis.read(data))!=-1){
                Thread.sleep(200);
                dout.write(data, 0,buffer);
                transfere+=buffer;
                pourcentage=(int) (transfere*100/f.length());
                System.out.println(pourcentage+"%");
                dout.flush();
            }
        }
        dis.close();
        
    }
    
    
    public void sendImage(int num) throws IOException{
        ImageIcon imageIcon=new ImageIcon(getListeImage().get(num-1));
        
   
        try {
            OutputStream outputStream=sc.getOutputStream();
            try (BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream)) {
                Image image=imageIcon.getImage();
                BufferedImage bufferedImage=new BufferedImage(image.getWidth(null),image.getHeight(null),BufferedImage.TYPE_INT_RGB);

                Graphics graphics=bufferedImage.createGraphics();
                graphics.drawImage(image,0,0,null);
                graphics.dispose();

                ImageIO.write(bufferedImage,"png",bufferedOutputStream);
            }

        } catch (IOException ex) {

        }
 
    }
    
    public void sendVideo(int num) throws FileNotFoundException, IOException, InterruptedException{
        File f=new File(getListeVideo().get(num-1));
        FileInputStream fis=new FileInputStream(f);
        InputStream dis=new DataInputStream(fis); 
        //byte[] data=dis.readAllBytes();
        byte[] data=new byte[4096];
        int buffer=0;
        int transfere=0;
        int pourcentage=0;
        OutputStream dout=new DataOutputStream(getSc().getOutputStream());

        while((buffer=dis.read(data))!=-1){
            //Thread.sleep(150);
            dout.write(data, 0,buffer);
            transfere+=buffer;
            pourcentage=(int) (transfere*100/f.length());
            System.out.println(pourcentage+"%");
            dout.flush();
        }
    }
    
    public int getChoice() throws IOException{
        System.out.println("Waiting for request...");
        DataInputStream choiceClient=new DataInputStream(getSc().getInputStream());
        int num=choiceClient.readInt();
        System.out.println(num);
        return num;
    }
    
    public void checkListToSend() throws IOException{
        DataInputStream choiceClient=new DataInputStream(getSc().getInputStream());
        int num=choiceClient.readInt();
        switch (num) {
            case 1 -> this.sendListMusicToClient();
            case 2 -> this.sendListImageToClient();
            case 3 -> this.sendListVideoToClient();
        }
        setMark(num);
    }
    
    public void sendMessage() throws IOException{
        DataOutputStream listToSend=new DataOutputStream(getSc().getOutputStream());
        String msg;
        switch (this.getMark()) {
            case 1 -> {
                msg="send music...";
                listToSend.writeUTF(msg);
                listToSend.flush();
            }
            case 2 -> {
                msg="send photo...";
                listToSend.writeUTF(msg);
                listToSend.flush();
            }
            case 3 -> {
                msg="send video...";
                listToSend.writeUTF(msg);
                listToSend.flush();
            }
            default -> {
                msg="Not found!!!...";
                listToSend.writeUTF(msg);
                listToSend.flush();
            }
        }
        
    }
    
    public void sendFile() throws IOException, FileNotFoundException, InterruptedException{
        switch (this.getMark()) {
            //case 1 -> this.sendMusicToPlay(this.getChoice());
            case 1 -> this.sendMusicToPlay(this.getChoice());
            case 2 -> this.sendImage(this.getChoice());
            case 3 -> this.sendVideo(this.getChoice());
        }
    }
    
    public String getTitleFile(String path){
        String result;
        String[] tabString=path.split("/");
        int size=tabString.length;
        result=tabString[size-1];
        return result;
    }
    
    
    public void addListMusic(String path){
        this.getListeMusic().add(path);
    }
    
    public ServerSocket getSs() {
        return ss;
    }

    public void setSs(ServerSocket ss) {
        this.ss = ss;
    }

    public Socket getSc() {
        return sc;
    }

    public void setSc(Socket sc) {
        this.sc = sc;
    }

    public ArrayList<String> getListeMusic() {
        return listeMusic;
    }

    public void setListeMusic(ArrayList<String> listeMusic) {
        this.listeMusic = listeMusic;
    }

    public ArrayList<String> getListeImage() {
        return listeImage;
    }

    public void setListeImage(ArrayList<String> listeImage) {
        this.listeImage = listeImage;
    }

    public int getMark() {
        return mark;
    }

    public void setMark(int mark) {
        this.mark = mark;
    }
    
    public ArrayList<String> getListeVideo() {
        return listeVideo;
    }

    public void setListeVideo(ArrayList<String> listeVideo) {
        this.listeVideo = listeVideo;
    }
    public static void main(String args[]) throws IOException, FileNotFoundException, InterruptedException{
            ServeurMedia sMedia=new ServeurMedia();
            sMedia.checkListToSend();
            sMedia.sendMessage();
            sMedia.sendFile();
    }
    
    
    
     
}
