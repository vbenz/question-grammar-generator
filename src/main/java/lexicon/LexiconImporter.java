package lexicon;

import eu.monnetproject.lemon.LemonModel;
import eu.monnetproject.lemon.LemonSerializer;
import eu.monnetproject.lemon.model.LexicalEntry;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class LexiconImporter {
  private Logger log = LogManager.getLogger(LexiconImporter.class);

  public LexiconImporter() {
  }

  public LemonModel loadModelFromDir(String dir) throws URISyntaxException, IOException {
    final LemonSerializer serializer = LemonSerializer.newInstance();
    LemonModel model = null;
    try (Stream<Path> paths = Files.walk(Paths.get(ClassLoader.getSystemResource(dir).toURI()))) {
      List<Path> list = paths
          .filter(Files::isRegularFile)
          .collect(Collectors.toList());
      for (Path file : list) {
        try {
          if (model == null) {
            model = serializer.read(new FileReader(file.toString()));
          } else {
            LemonModel lm = serializer.read(new FileReader(file.toString()));
            mergeModels(model, lm);
          }
        } catch (FileNotFoundException e) {
          log.error("FileNotFoundException: Could not read file {}", file);
        }
      }
      assert model != null;
      return model;
    }
  }

  private void mergeModels(LemonModel model, LemonModel lm) {
    for (eu.monnetproject.lemon.model.Lexicon lexicon : lm.getLexica()) {
      for (LexicalEntry lexicalEntry : lexicon.getEntrys()) {
        model.getLexica().iterator().next().addEntry(lexicalEntry);
      }
    }
  }
}
