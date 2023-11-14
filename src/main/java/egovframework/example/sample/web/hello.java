package egovframework.example.sample.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

@RestController
@RequestMapping("/restapi")
public class hello {
//	@RequestMapping(value = "/helloworld", method = RequestMethod.GET)
//	@GetMapping("")
//	public String defaultPage() {
//		String result="defaulttt!!";
//		System.out.println(result);
//		return result;
//	}
//	
//	@GetMapping("/helloworld")
//	public String helloWorld() {
//		String result="helloworldddd : ";
//		System.out.println(result);
//		return result;
//	}
	@GetMapping("/{name}")
	public String sayHello(@PathVariable String name) {
		String result="Hello eGovFramework!! name : " + name;  
		return result;
	}
}
