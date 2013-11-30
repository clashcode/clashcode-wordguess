package actors

import scala.collection.mutable

import clashcode.logic.Player
import akka.actor.ActorRef

case class ActorPlayer(player: Player, actor: ActorRef)

trait ActorPlayers {
  val actorPlayers = mutable.Buffer[ActorPlayer]()

  def removeExistingActorPlayerNamed(playerName: String) {
    findActorPlayer(playerName) map { actorPlayer =>
      actorPlayers -= actorPlayer
    }
  }

  def findActorPlayerCreatingIfNeeded(actor: ActorRef, playerName: String): ActorPlayer = {
    val optExisting = findActorPlayer(actor)
    optExisting getOrElse {
      val newActorPlayer = ActorPlayer(Player(playerName), actor)
      actorPlayers += ActorPlayer(Player(playerName), actor)
      newActorPlayer
    }
  }

  def findActorPlayer(playerName: String): Option[ActorPlayer] = {
    actorPlayers.find(actorPlayer => actorPlayer.player.name == playerName)
  }

  def findActorPlayer(player: Player): Option[ActorPlayer] = {
    actorPlayers.find(actorPlayer => actorPlayer.player == player)
  }

  def findActorPlayer(actor: ActorRef): Option[ActorPlayer] = {
    actorPlayers.find(actorPlayer => actorPlayer.actor == actor)
  }

}