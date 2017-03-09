export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ timeStamp, username, command, contents }) {
    this.timeStamp = timeStamp
    this.username = username
    this.command = command
    this.contents = contents
  }

  toJSON () {
    return JSON.stringify({
      timestamp: this.timeStamp,
      username: this.username,
      command: this.command,
      contents: this.contents
    })
  }

  toString () {
    const message = this.timeStamp + ` <` + this.username + `> (` + this.command + `): ` + this.contents
    return message
  }
}
