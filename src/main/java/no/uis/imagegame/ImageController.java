package no.uis.imagegame;

import java.io.*;
import java.util.*;

import static java.lang.String.format;

import no.uis.websocket.SocketMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import no.uis.party.PartyManager;
import no.uis.party.Party;
import no.uis.players.Player;
import no.uis.repositories.PlayerRepository;

/**
 * Class controlling the front end view of the image game and receives user input from there.
 */
@Controller
public class ImageController {

    //Load list of images in my scattered_images folder
    @Value("classpath:/static/images/scattered_images/*")
    private Resource[] resources;

    //Initialize my label reader
    ImageLabelReader labelReader = new ImageLabelReader("src/main/resources/static/label/label_mapping.csv",
            "src/main/resources/static/label/image_mapping.csv");

    @Autowired
    private SimpMessageSendingOperations messageTemplate;


    /**
     * Sends a socket message to the guesser through the party ID when the proposer has
     * chosen an image for starting the game. This tells the guesser what party they belong to.
     *
     * @param partyId     Destination variable indicating the socket message is sent to the specific party route.
     * @param chatMessage Socket message containing the party ID.
     * @author Alan Rostem
     */
    @MessageMapping("/party/{partyId}/respToGuesser")
    public void respondToGuesser(@DestinationVariable String partyId, @Payload SocketMessage chatMessage) {
        Party party = PartyManager.getParty(partyId);
        if (party == null) {
            return;
        }

        SocketMessage msg = new SocketMessage();
        msg.setSender(party.getProposer().getId());
        msg.setType("JOIN_PARTY");

        HashMap<String, Object> guesserContent = new HashMap<>();
        guesserContent.put("role", "GUESSER");
        guesserContent.put("partyId", "" + partyId);
        guesserContent.put("time", party.getGame().getTime());

        guesserContent.put("selectedlabel", chatMessage.getContent());
        msg.setContent(guesserContent);
        party.getGuesser().sendData(msg, messageTemplate);
        
        HashMap<String, Object> proposerContent = new HashMap<>();
        proposerContent.put("state", party.getProposer().getPlayerStatus());
        proposerContent.put("score", party.getGame().getScore());
        proposerContent.put("time", party.getGame().getTime());
        proposerContent.put("segments", party.getGame().getGuesserSegments());
        msg.setContent(proposerContent);
        party.getProposer().sendData(msg, messageTemplate);
    }

    /**
     * Initialize the websocket handler and send a response to the client subscription
     *
     * @param partyId        Destination variable indicating the socket message is sent to the specific party route.
     * @param chatMessage    Socket message containing the party ID.
     * @param headerAccessor Automated object by SpringBoot to configure websocket sessions with
     * @author Alan & Gregoire
     */
    @MessageMapping("/party/{partyId}/addUser")
    public void addUser(@DestinationVariable String partyId, @Payload SocketMessage chatMessage,
                        SimpMessageHeaderAccessor headerAccessor) {
//        String currentRoomId = (String) headerAccessor.getSessionAttributes().put("room_id", partyId);
//        if (currentRoomId != null) {
//            SocketMessage leaveMessage = new SocketMessage();
//            leaveMessage.setType("LEAVE");
//            leaveMessage.setSender(chatMessage.getSender());
//            messageTemplate.convertAndSend(format("/channel/%s", currentRoomId), leaveMessage);
//        }
//        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
//        
//        messageTemplate.convertAndSend(format("/channel/%s", partyId), chatMessage);
//        
        Party party = PartyManager.getParty(partyId);
        if (party == null) {
            return;
        }
        SocketMessage msg = new SocketMessage();
        msg.setSender(party.getProposer().getId());
        msg.setType("JOIN_PARTY");
        
        HashMap<String, Object> guesserContent = new HashMap<>();
        guesserContent.put("state", party.getGuesser().getPlayerStatus());
        guesserContent.put("score", party.getGame().getScore());
        guesserContent.put("time", party.getGame().getTime());
        guesserContent.put("segments", party.getGame().getGuesserSegments());
        
        msg.setContent(guesserContent);
        party.getGuesser().sendData(msg, messageTemplate);
        
    }


    /**
     * Proposer init, loads image and sends it to the view.
     *
     * @param model Model with attributes used to send data to the view and parse with Thymeleaf
     * @param modelname Image label name
     * @param partyId   ID of the party the proposer belongs to
     * @param userName  Name of the proposer used to display on the front. // TODO: Do we use it on the front?
     * @return The configured model and view
     * @author Eirik & Gregoire
     */
    @RequestMapping("/proposer")
    public ModelAndView showImage(ModelAndView model,
                                  @ModelAttribute("selectedlabel") Object modelname,
                                  @RequestParam(value = "partyId", required = false, defaultValue = "-1") String partyId,
                                  @RequestParam(value = "username", required = false, defaultValue = "-1") String userName) {

        String name = modelname.toString() != null ? modelname.toString() : "cinema";

        String[] files = labelReader.getImageFiles(name);
        String image_folder_name = getImageFolder(files);
        Party party = PartyManager.getParty(partyId);
        Player proposer = party.getProposer();

        model.addObject("userId", proposer.getId());
        model.addObject("partyId", partyId);
        model.addObject("username", userName);

        int countTotalSegments = party.getGame().getProposerSegments().size();

        if (countTotalSegments==0) {
        	// finds number of segments per image
        	countTotalSegments = -1 + Objects.requireNonNull(new File("src/main/resources/static/images/scattered_images/" + image_folder_name).list()).length;
        	PartyManager.getParty(partyId).getGame().setImage(name, countTotalSegments);
        }

        ArrayList<String> imageSegments = new ArrayList<>();        	
        for (int i = 0; i < countTotalSegments; ++i) {
            imageSegments.add("images/scattered_images/" + image_folder_name + "/" + i + ".png");
        }

        model.addObject("listimages", imageSegments);
        return model;
    }

    /**
     * Receive the client socket message every time a player performs and action
     * on the game page. The messages are send to the respective party and game logic
     * is performed there.
     *
     * @param partyId ID for the party the player belongs to
     * @param message Socket message containing data sent by user
     * @author Alan & Gregoire
     */
    @MessageMapping("/party/{partyId}/update")
    public void receiveSocketUpdate(@DestinationVariable String partyId, SocketMessage message) {
        PartyManager.getParty(partyId).receiveUpdateFromFront(message, messageTemplate);
    }

    /**
     * Proposer image selection view init; Allows the user choose a picture which
     * then sends a message to the respective guesser of the party.
     *
     * @param partyId Respective ID of the party the proposer belongs to
     * @param userName Username of the proposer
     * @return Model and view with attributes used to send data to the view and parse with Thymeleaf
     * @author Eirik & Gregoire
     */
    @RequestMapping("/proposerImageSelection")
    public ModelAndView showLabels(
            @RequestParam(value = "partyId", required = false, defaultValue = "-1") String partyId,
            @RequestParam(value = "username", required = false, defaultValue = "-1") String userName) {
        ModelAndView model = new ModelAndView("proposerImageSelection");
        ArrayList<String> imageLabels = getAllLabels(labelReader);
        Party party = PartyManager.getParty(partyId);

        Player guesser = party.getProposer();
        String userId = guesser.getId();
        model.addObject("listlabels", imageLabels);
        model.addObject("userId", userId);
        model.addObject("partyId", partyId);
        model.addObject("username", userName);
        return model;
    }

    /**
     * Guesser game view; The game actions are applied by the guesser here.
     *
     * @param partyId Respective ID of the party the proposer belongs to
     * @param userName Name of the guesser
     * @return Model and view with attributes used to send data to the view and parse with Thymeleaf
     * @author Eirik & Gregoire
     */
    @RequestMapping(value = "/guesser")
    public ModelAndView showImageGuesser(
            @RequestParam(value = "partyId", required = false, defaultValue = "-1") String partyId,
            @RequestParam(value = "username", required = false, defaultValue = "-1") String userName) {

        ModelAndView model = new ModelAndView("guesser");
        model.addObject("userId", PartyManager.getParty(partyId).getGuesser().getId());
        model.addObject("partyId", partyId);
        model.addObject("username", userName);

        String name = PartyManager.getParty(partyId).getGame().getImageName();
        String[] files = labelReader.getImageFiles(name);
        String image_folder_name = getImageFolder(files);
        // finds number of segments per image
        int countTotalSegments = -1 + Objects.requireNonNull(new File("src/main/resources/static/images/scattered_images/" + image_folder_name).list()).length;
        ArrayList<String> imageSegments = new ArrayList<>();
        for (int i = 0; i < countTotalSegments; ++i) {
            imageSegments.add("images/scattered_images/" + image_folder_name + "/" + i + ".png");
        }

        model.addObject("listimages", imageSegments);
        return model;
    }

    /**
     * Retrieve the image folder name for an array of images.
     * @param files Array of the file names
     * @return String containing the image folder name
     */
    private String getImageFolder(String[] files) {
        String image_folder_name = "";
        for (String file : files) {
            String folder_name = file + "_scattered";
            for (Resource r : resources) {
                if (folder_name.equals(r.getFilename())) {
                    image_folder_name = folder_name;
                    break;
                }
            }
        }
        return image_folder_name;
    }

    /**
     * Retrieve all the labels from an ImageLabelReader
     * @param ilr The image label reader
     * @return Array list of all the labels
     * @see ImageLabelReader
     */
    private ArrayList<String> getAllLabels(ImageLabelReader ilr) {
        ArrayList<String> labels = new ArrayList<String>();
        for (Resource r : resources) {
            String fileName = r.getFilename();
            String fileNameCorrected = fileName.substring(0, fileName.lastIndexOf('_'));
            String label = ilr.getLabel(fileNameCorrected);
            labels.add(label);
        }
        return labels;
    }

}
