package tech.artisanhub.controller;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;
import tech.artisanhub.aSyncTaskManager.AppConfig;
import tech.artisanhub.aSyncTaskManager.AsyncTask;
import tech.artisanhub.fileHandler.FileOperations;
import tech.artisanhub.shapeletTrainer.LearnShapelets;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@Controller
@RequestMapping("/learner")
public class LearnerController {

    @Autowired
    private SimpMessagingTemplate template;


    @RequestMapping(value = "/start", method = RequestMethod.GET)
    public String startProcess(@RequestParam("dataset") String name, ModelMap model) {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
        ctx.register(AppConfig.class);
        ctx.refresh();
        AsyncTask task = ctx.getBean(AsyncTask.class);
        try {
            task.doAsyncTask(name,template);
        } catch (Exception e) {
            System.out.println(name + "Leaning has been interrupted");
            //Send an email
        }

        model.addAttribute("test", name);
        return "googleChartsVisualizer";


    }

    @RequestMapping(value = "/results", method = RequestMethod.GET)
    public
    @ResponseBody
    String getResult(@RequestParam("dataset") String datasetName) {

        try {
            return FileOperations.getGeneratedResults(datasetName).toJSONString();
        } catch (IOException e) {
            return "Error while reading the file";
        } catch (ParseException e) {
            return "Error file parsing the json string";
        } catch (ParserConfigurationException e) {
            return "Error while reading from the properties.xml file";
        } catch (SAXException e) {
            return "Error while reading from the properties.xml file";
        }

    }

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public Greeting greeting(HelloMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return new Greeting("Hello, " + message.getName() + "!");
    }

    public void fireGreeting() {
        System.out.println("Fire");
        this.template.convertAndSend("/topic/greetings", new Greeting("Fire"));
    }

}