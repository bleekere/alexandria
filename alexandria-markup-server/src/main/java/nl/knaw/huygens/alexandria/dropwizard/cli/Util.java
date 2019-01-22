package nl.knaw.huygens.alexandria.dropwizard.cli;

/*-
 * #%L
 * alexandria-markup-server
 * =======
 * Copyright (C) 2015 - 2019 Huygens ING (KNAW)
 * =======
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Util {
  private static final Logger LOG = LoggerFactory.getLogger(Util.class);

  public static String detectDotPath() {
    String[] options = System.getProperty("os.name").contains("Windows")
        ? new String[]{"where dot.exe", "which dot"}
        : new String[]{"which dot", "where dot.exe"};
    for (String detectionCommand : options) {
      try {
        final Process process = Runtime.getRuntime().exec(detectionCommand);
        try (BufferedReader processReader = new BufferedReader(new InputStreamReader(process.getInputStream(), Charset.defaultCharset()))) {
          final CompletableFuture<Optional<String>> path = CompletableFuture.supplyAsync(() -> processReader.lines()
              .map(String::trim)
              .filter(l -> l.toLowerCase().contains("dot"))
              .findFirst());
          process.waitFor();
          final String dotPath = path.get().get();
          LOG.debug("Detected GraphViz' dot at '" + dotPath + "'");
          return dotPath;
        }
      } catch (Throwable t) {
        return null;
      }
    }
    return null;
  }

}
