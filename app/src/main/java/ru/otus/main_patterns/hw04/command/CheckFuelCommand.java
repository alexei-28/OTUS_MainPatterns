package ru.otus.main_patterns.hw04.command;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hw04.Fuel;
import ru.otus.main_patterns.hw04.exceptions.NotEnoughFuelException;
import ru.otus.main_patterns.hw04.interfaces.Fuelable;

/*-
Конкретные команды реализуют различные запросы, следуя общему интерфейсу команд. Обычно, команда не
делает всю работу самостоятельно, а лишь передаёт вызов получателю — определённому объекту бизнес-логики.
Параметры, с которыми команда обращается к получателю, следует хранить в виде полей. В большинстве случаев,
объекты команд можно сделать неизменяемым, предавая в них все необходимые параметры только через конструктор.

Команда проверяет есть или нет топлива у движущегося средства.
Должен принять на вход интерфейс(Fuelable), который поддерживает получение информации о том сколько сейчас есть топлива и сколько будет израсходовано топлива.
*/
public class CheckFuelCommand implements Command {
  private final Fuelable fuelable;

  private static final Logger logger = LoggerFactory.getLogger(CheckFuelCommand.class);

  public CheckFuelCommand(Fuelable fuelable) {
    this.fuelable = fuelable;
  }

  @Override
  public void execute() {
    logger.debug("Execute");
    Fuel fuel = fuelable.getFuel();
    Fuel consumedFuel = fuelable.getConsumedFuel();
    if (fuel.getValue() - consumedFuel.getValue() < 0) {
      throw new NotEnoughFuelException();
    }
  }
}
