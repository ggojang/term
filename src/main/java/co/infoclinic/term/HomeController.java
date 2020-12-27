package co.infoclinic.term;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

/**
 * Handles requests for the application home page.
 */
@RestController
public class HomeController {
	
    Logger log = LoggerFactory.getLogger(HomeController.class);
	
	@RequestMapping(value = "/api", method = RequestMethod.GET)
    public ModelAndView api() {
      return new ModelAndView("redirect:/swagger-ui.html");
    }
    
    
	@RequestMapping(value = "", method = RequestMethod.GET)
	public ModelAndView index(Locale locale, Model model) {
		
		return new ModelAndView("index");
	}
	
	
}
