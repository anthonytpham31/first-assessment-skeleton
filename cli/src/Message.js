export class Message {
  static fromJSON (buffer) {
    return new Message(JSON.parse(buffer.toString()))
  }

  constructor ({ timestamp, username, command, contents }) {
    this.timestamp = timestamp
    this.username = username
    this.command = command
    this.contents = contents
  }

  toJSON () {
    return JSON.stringify({
      timestamp: this.timestamp,
      username: this.username,
      command: this.command,
      contents: this.contents
    })
  }

  toString () {
    if (this.command === null) {
      this.command = 'connect'
      const message = this.timestamp + ` <` + this.username + `> (` + this.command + `): ` + this.contents
      return message
    } else if (this.command.charAt(0) === '@') {
      const message = this.timestamp + ` <` + this.username + `> (whispers): ` + this.contents
      return message
    } else if (this.command === 'users') {
      const message = this.timestamp + this.contents
      return message
    } else {
      const message = this.timestamp + ` <` + this.username + `> (` + this.command + `): ` + this.contents
      return message
    }
  }
}
