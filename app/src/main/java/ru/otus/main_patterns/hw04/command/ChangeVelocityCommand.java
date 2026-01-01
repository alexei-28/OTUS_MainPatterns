package ru.otus.main_patterns.hw04.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw04.interfaces.Rotatable;
import ru.otus.main_patterns.hw04.model.Direction;

/*-
Конкретные команды реализуют различные запросы, следуя общему интерфейсу команд. Обычно, команда не
делает всю работу самостоятельно, а лишь передаёт вызов получателю — определённому объекту бизнес-логики.
Параметры, с которыми команда обращается к получателю, следует хранить в виде полей. В большинстве случаев,
объекты команд можно сделать неизменяемым, предавая в них все необходимые параметры только через конструктор.
*/
public class ChangeVelocityCommand implements Command {
  private final Rotatable rotatable;
  private static final Logger logger = LoggerFactory.getLogger(ChangeVelocityCommand.class);

  public ChangeVelocityCommand(Rotatable rotatable) {
    this.rotatable = rotatable;
  }

  @Override
  public void execute() throws InterruptedException {
    logger.debug("Execute");
    Direction direction = rotatable.getDirection();
    Direction nextDirection = direction.next(rotatable.getAngularVelocity());
    rotatable.setDirection(nextDirection);
  }
}
