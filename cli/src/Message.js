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
    console.log(this.timestamp + 'toJSON')
    console.log(this.username + 'toJSON')
    console.log(this.command + 'toJSON')
    console.log(this.contents + 'toJSON')
    return JSON.stringify({
      timestamp: this.timestamp,
      username: this.username,
      command: this.command,
      contents: this.contents
    })
  }

  toString () {
    if (this.command.charAt(0) === '@') {
      const message = this.timestamp + ` <` + this.username + `> (whisper): ` + this.contents
      return message
    } else if (this.command === 'users') {
      const message = this.timestamp + ` ` + this.contents
      return message
    } else if (this.command === 'broadcast') {
      const message = this.timestamp + ` <` + this.username + `> (all): ` + this.contents
      console.log(message + 'toString')
      console.log(this.timestamp + 'toString')
      console.log(this.username + 'toString')
      console.log(this.command + 'toString')
      console.log(this.contents + 'toString')
      return message
    } else {
      const message = this.timestamp + ` <` + this.username + `> (` + this.command + `): ` + this.contents
      return message
    }
  }
}
