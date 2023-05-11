package ai.qwiet.springbootkotlinwebgoat

import org.springframework.http.HttpHeaders
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import java.io.InputStreamReader
import java.io.BufferedReader
import java.io.File


@RestController
class HelloController {
  @GetMapping("/")
  fun index(): String {
    return "Greetings from Spring Boot!"
  }

  @GetMapping("/greet")
  fun greet(@RequestParam("username") username: String): String {
    // vulnerability: XSS
    return "Greetings ${username}!"
  }

  fun parseParams(name: String, msg: String): Map<String, String?> {
    val checkedName = name.takeUnless { it.contains('\\') }?.ifBlank { "default_name" }
    val checkedMsg = msg.ifBlank { "default_msg" }
    return mapOf("parsed_name" to checkedName, "parsed_msg" to checkedMsg)
  }

  @GetMapping("/exec")
  fun exec(@RequestParam("cmd") cmd: String): ResponseEntity<String> {
    var out = "NOP"
    if (cmd != "nop") {
      // vulnerability: Remote Code Execution
      val proc = Runtime.getRuntime().exec(cmd)
      val lineReader = BufferedReader(InputStreamReader(proc.getInputStream()));
      val output = StringBuilder()
      lineReader.lines().forEach { line ->
          output.append(line + "\n")
      }
      out = "Did execute command `" + cmd + "`, got stdout:" + output;
    }
    return ResponseEntity(out, HttpStatus.OK)
  }


  @GetMapping("/touch_file")
  fun touchFile(@RequestParam("name") name: String, @RequestParam("msg") msg: String): ResponseEntity<String> {
    if (name == null || msg == null) {
      return ResponseEntity("The `name` & `msg` parameters have to be set.", HttpStatus.OK)
    } else {
      val parsedParams = parseParams(name, msg)
      val fullPath = "/tmp/http4kexample/" + parsedParams["parsed_name"]
      val finalMsg = "MESSAGE: " + parsedParams["parsed_msg"]
      // vulnerability: Directory Traversal
      File(fullPath).writeText(finalMsg)
      return ResponseEntity("Did write message `" + finalMsg + "` to file at `" + fullPath + "`", HttpStatus.OK)
    }
  }

  @GetMapping("/debug")
  fun debug(@RequestParam("url") url: String): ResponseEntity<String> {
    val headers = HttpHeaders()
    headers.add("Location", url)
    // vulnerability: Open Redirect
    return ResponseEntity(headers, HttpStatus.FOUND)
  }


  @GetMapping("/render_html")
  fun renderHtml(@RequestParam("name") name: String): ResponseEntity<String> {
    // vulnerability: XSS
    val out = StringBuilder().append("<h1>Hello there, ").append("$name").append("!</h1>").toString()
    return ResponseEntity(out, HttpStatus.OK)
  }
}
