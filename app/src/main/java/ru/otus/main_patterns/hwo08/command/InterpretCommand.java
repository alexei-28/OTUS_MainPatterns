package ru.otus.main_patterns.hwo08.command;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.otus.main_patterns.hwo08.IoC;
import ru.otus.main_patterns.hwo08.config.GameConfig;
import ru.otus.main_patterns.hwo08.dto.Order;
import ru.otus.main_patterns.hwo08.service.GameConfigService;
import ru.otus.main_patterns.hwo08.service.ScopeService;

public class InterpretCommand implements Command {
  private final Order order;
  private final GameConfig gameConfig;

  private static final Logger logger = LoggerFactory.getLogger(InterpretCommand.class);

  public InterpretCommand(Order order) {
    this.order = order;
    gameConfig = GameConfigService.get();

    // Обновляем базовую стратегию("update.ioc.resolve.dependency.strategy") IoC, чтобы она умела
    // искать в нашем scopesMap.
    UpdateIocResolveDependencyStrategyCommand updateCmd =
        IoC.resolve(
            "update.ioc.resolve.dependency.strategy",
            (Function<BiFunction<String, Object[], Object>, BiFunction<String, Object[], Object>>)
                oldStrategy ->
                    (dependency, args) -> {
                      if (ScopeService.getGlobalScope().containsKey(dependency)) {
                        Function<Object[], Object> strategy =
                            ScopeService.getGlobalScope().get(dependency);
                        return strategy.apply(args);
                      }
                      return oldStrategy.apply(dependency, args);
                    });
    updateCmd.execute();
  }

  @Override
  public void execute() {
    validate(order);

    Command command = IoC.<Command>resolve("create.command", order);

    // Выполняем команду queueCommand, которая поместит команду command в очередь команд игры.
    Command queueCommand = IoC.<QueueCommand>resolve("queue.command", command);
    queueCommand.execute();
  }

  // Проверяем, что player может запускать операцию из сообщения
  private void validate(Order order) {
    Map<String, GameConfig.Game> games = gameConfig.getGames();
    GameConfig.Game game = games.get(order.getGameId());
    Map<String, GameConfig.Player> players = game.getPlayers();
    GameConfig.Player player = players.get(order.getPlayerId());
    List<String> operations = player.getOperations();
    boolean allowed = operations.contains(order.getOperationId().toString());
    if (!allowed) {
      throw new SecurityException(
          "Player '"
              + order.getPlayerId()
              + "' is not allowed to execute operation "
              + order.getOperationId());
    }
  }
}
