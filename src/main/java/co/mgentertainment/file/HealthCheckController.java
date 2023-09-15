package co.mgentertainment.file;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author larry
 * @createTime 2023/6/30
 * @description HealthCheckController
 */
@Controller
public class HealthCheckController {

    @GetMapping("/checkPreload")
    @ResponseBody
    public String healthCheck() {
        return "success";
    }
}
