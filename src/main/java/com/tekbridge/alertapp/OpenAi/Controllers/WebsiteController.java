package com.tekbridge.alertapp.OpenAi.Controllers;
import org.springframework.ai.*;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


public class WebsiteController {

//    private final ChatClient ai;
//
//    public WebsiteController(ChatClient ai){
//        this.ai = ai;
//    }
//
//    @GetMapping(value = "/websites/{topic}", produces = MediaType.TEXT_HTML_VALUE)
//    public String generate(@PathVariable String topic) {
//        String prompt =  """
//                        Return valid HTML5 for a {topic} single-page website.
//                        Make the page look visually appealing by using different colors and fonts.
//                        Also, provide valid copy for the individual sections.""";
//        PromptTemplate template = new PromptTemplate(prompt);
//        template.add("topic",topic);
//        ChatResponse chatResponse = this.ai.call(template.create());
//        return ai.call(
//           template.render()
//        );
//    }
}


//I need single flyer picture design for my brand matrend