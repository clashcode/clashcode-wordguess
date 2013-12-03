package clashcode.logic

import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter

class GameLogicSuite extends FunSuite with BeforeAndAfter {

  val testWords = Seq("hello", "world")

  var logic: GameLogic = _

  def makeGameState(words: String*) = {
    val wordStates = words.map { WordState(_, solved = false) }
    new GameState(wordStates.toList)
  }

  before {
    logic = new GameLogic() {
      val gameState = makeGameState("hello", "world")
      def onGameWon(player: Player, game: Game) {}
      def onGameLost(player: Player, game: Game) {}
    }
  }

  val player = Player("testPlayer")

  test("create a game for player") {
    val game = logic.createGame(player)
  }

  test("valid length of word") {
    val game = logic.createGame(player)
    assert(testWords(game.wordIdx).length === game.status.letters.length)
  }

  test("initially, word is unsolved") {
    val game = logic.createGame(player)
    assert(game.status.letters.forall(!_.isDefined), "Should not contain any defined letter")
  }

  test("initially there is no game for a player") {
    assert(logic.getGame(player) === None)
  }

  test("can ask for a player's game") {
    val game = logic.createGame(player)
    assert(logic.getGame(player) === Some(game))
  }

  test("given 2 words, 2 players should get 2 different words") {
    val player1 = player
    val player2 = Player("TestPlayer2")
    val game1 = logic.createGame(player1)
    val game2 = logic.createGame(player2)
    assert(game1 != game2, "Games should be different")
    assert(game1.wordIdx != game2.wordIdx, "Word indices should be different")
  }

  test("initially game is unsolved") {
    val game = logic.createGame(player)
    assert(game.isSolved === false)
  }

  test("making a wrong guess should decrease my remaining tries") {
    val game = logic.createGame(player)
    val triesBefore = game.status.remainingTries
    logic.makeGuess(player, letter = 'x')
    val triesAfter = game.status.remainingTries
    assert(triesAfter === triesBefore - 1, "Tries should have decreased by 1")
  }

  test("making a correct guess should reveal new letter in status") {
    val game = logic.createGame(player)
    val revealedBefore = game.status.letters.count(_.isDefined)
    logic.makeGuess(player, letter = 'o')
    val revealedAfter = game.status.letters.count(_.isDefined)
    assert(revealedAfter === revealedBefore + 1, "One new letter should have been revealed")
  }

  test("winning a game should result in callback") {
    val player = Player("testPlayer")
    val logic = new GameLogic() {
      val gameState = makeGameState("hello")
      var wasCalled = false
      def onGameWon(player: Player, game: Game) {
        wasCalled = true
      }
      def onGameLost(player: Player, game: Game) {}
    }
    val game = logic.createGame(player)
    logic.makeGuess(player, 'h')
    logic.makeGuess(player, 'e')
    logic.makeGuess(player, 'l')
    logic.makeGuess(player, 'o')
    assert(logic.wasCalled, "Should have been called upon winning the game")
  }

  test("loosing a game should result in callback") {
    val player = Player("testPlayer")
    val logic = new GameLogic() {
      val gameState = makeGameState("abc")
      var wasCalled = false
      def onGameWon(player: Player, game: Game) {}
      def onGameLost(player: Player, game: Game) {
        wasCalled = true
      }
    }
    val game = logic.createGame(player)
    (1 to logic.triesPerGame).foreach(_ => logic.makeGuess(player, 'x'))
    assert(logic.wasCalled, "Should have been called upon loosing the game")
  }

  test("be case insensitive") {
    val player = Player("testPlayer")
    val logic = new GameLogic() {
      val gameState = makeGameState("hello")
      var wasCalled = false
      def onGameWon(player: Player, game: Game) {
        wasCalled = true
      }
      def onGameLost(player: Player, game: Game) {}
    }
    val game = logic.createGame(player)
    ("hElO").foreach(c => logic.makeGuess(player, c))
    assert(logic.wasCalled, "The game should have been won")
  }

  trait HelloGameLogicTest {
    val player = Player("testPlayer")
    val logic = new GameLogic() {
      val gameState = makeGameState("hello")
      def onGameWon(player: Player, game: Game) {}
      def onGameLost(player: Player, game: Game) {}
    }
    val game = logic.createGame(player)
    def guessLetters(letters: String) = letters.foreach(c => logic.makeGuess(player, c))
  }

  test("a guessed word should added to the solved words") {
    new HelloGameLogicTest {
      guessLetters("helo")
      assert(logic.solvedWordIndexes === Seq(0))
    }
  }

  test("when the game is won, it's removed from the list of games") {
    new HelloGameLogicTest {
      guessLetters("helo")
      assert(logic.getGame(player) === None, "Game should not be found anymore")
    }
  }

  test("when the game is lost, it's removed from the list of games") {
    new HelloGameLogicTest {
      guessLetters("xqpkn")
      assert(logic.getGame(player) === None, "Game should not be found anymore")
    }
  }

  test("when the last word has been guessed, there are no more words remaining") {
    new HelloGameLogicTest {
      guessLetters("helo")
      assert(logic.hasRemainingWords === false)
    }
  }

  test("removing game of player") {
    val game = logic.createGame(player)
    assert(logic.getGame(player) === Some(game), "Player should have game")
    logic.removeGameOf(player)
    assert(logic.getGame(player) === None, "Player should NOT have game anymore")
  }

  test("see game words while playing") {
    new HelloGameLogicTest {
      guessLetters("el")
      val gameWords = logic.gameWords
      assert(gameWords === Seq(GameWord(0, "_ell_", playing = true, solved = false)))
    }
  }

  test("see game words before playing") {
    val gameWords = logic.gameWords
    assert(gameWords === Seq(
      GameWord(0, "_____", playing = false, solved = false),
      GameWord(1, "_____", playing = false, solved = false)))
  }

  test("renaming a player's game") {
    new HelloGameLogicTest {
      val game1 = logic.getGame(Player("testPlayer"))
      logic.renameGamePlayerName("testPlayer", "newPlayerName")
      val game2 = logic.getGame(Player("newPlayerName"))
      assert(game1.isDefined, "A game should have been found")
      assert(game1 === game2, "Should be the same game, the player was just renamed")
    }
  }

}