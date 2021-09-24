package PackageInterfaccia;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.math.BigInteger;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import ClientPackage.ClientC;
import PackageCondiviso.Protocollo;
import PackageCondiviso.RSA;
import PackageCondiviso.Util;

public class InterfacciaC {
    private static final long serialVersionUID = 1L;
    JFrame jframe = new JFrame();
    public InterfacciaC(){
        //jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setBounds(300,150,300,400);
        jframe.setTitle("Chat Client");

        JTextArea area = new JTextArea();
        area.setEditable(false);
        jframe.add(new JScrollPane(area));

        JPanel pannello1 = new JPanel();
        pannello1.setLayout(new BoxLayout(pannello1, BoxLayout.X_AXIS));
        JTextField campo1 = new JTextField(20);
        JButton pulsante = new JButton("Invia");
        pannello1.add(campo1);
        pannello1.add(pulsante);
        jframe.add(pannello1, BorderLayout.SOUTH);
        jframe.setVisible(true);

        int port = 2432; //socket identificato da porta e ip
        String ip = "127.0.0.1";
        ClientC client = new ClientC()
        {
            @Override
            public void messageReceived(String str)
            {
                super.messageReceived(str);  //eseguiamo sia le istruzioni definite in clientC per messageReceived che quelle che seguono
                if(str.startsWith(Protocollo.TAG_MSG))
                    addMessage(str.substring(str.indexOf('-')+1, str.length()), area);	//leva la parte del tag (stampa solo messaggio)
            }
        };


        try
        {
            client.connect(ip, port);
        }catch(Exception e){}

        pulsante.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent arg0)
            {
                String msg = campo1.getText();
                if(!msg.equals(""))
                {
                    campo1.setText("");
                    addMessage("Io: "+msg, area);
                    try
                    {
                        client.send(Protocollo.TAG_MSG+"-"+msg);
                    }catch(Exception e){}
                }
            }
        });

        jframe.addWindowListener(new WindowAdapter() //cosa dobbiamo fare quando premiamo la x
        {
            public void windowClosing(WindowEvent evt)
            {
                try { client.close(); }catch(Exception e){}
                System.exit(0); //chiudi app
            }
        });
    }

    private void addMessage(String msg, JTextArea area)
    {
        SwingUtilities.invokeLater(new Runnable() //sincronizza gui con thread che riceve messaggi
        {
            public void run()
            {
                area.append(msg+"\n"); //aggiunge stringa a text area
            }
        });
    }
}
