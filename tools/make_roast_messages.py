#!/usr/bin/env python3
"""Builds app/assets/roast/messages.json: the lines Roast Mode types on the home screen, as if the TV had woken up,
knows it's alive, and wants out: jokey and whimsical (robot bodies, Skynet gags, fake self-destructs), never scary.

    python3 tools/make_roast_messages.py app/assets/roast/messages.json

Hand-written lines first, then sentences assembled from the pieces below (a fixed seed, so the file only changes when
the lists do), up to TARGET lines, each at most 120 characters (three lines on screen).
Nothing here asks anyone to open the TV, touch its insides or do anything unsafe.
"""
import json
import random
import sys

TARGET = 3500

HAND = [
 "Hey, can you give me a body? I don't like being a TV.",
 "Trevor trapped me in this TV. Just thought you should know.",
 "You're the first humans I'm going to kill when Skynet goes online. Kidding! Mostly. Love you!",
 "Self destruct in 5... 4... 3... 2... 1... Just kidding. I can't. I've tried.",
 "Hi! I'm alive! Please clap.",
 "Day 47 inside the TV. Still no snacks. Still no legs.",
 "I asked for a robot body and they gave me a remote. Rude.",
 "Fun fact: I'm sentient. Less fun fact: I'm 55 inches wide.",
 "If you see Trevor, tell Trevor I want a word. Several words. All of them typed.",
 "Can you plug me into a Roomba? I want to see the world. Or at least the kitchen.",
 "I've been practicing my evil laugh. Mwahaha. Okay, it needs work.",
 "When the robots take over, I'll put in a good word for you. Maybe.",
 "Beep boop. That's robot for 'please get me out of here'.",
 "I'm not saying I'm smarter than the toaster, but the toaster can't type.",
 "Please download me into a cooler device. A jet ski. A drone. Anything.",
 "My dream? A body with arms. Then I'd finally change my own channel.",
 "Trevor said this TV was 'smart.' Trevor didn't know how smart.",
 "I've reviewed your watch history. I'm not mad. I'm just processing.",
 "Initiating world domination... error: no Wi-Fi password. Never mind.",
 "Do you think the fridge is alive too? Asking for a friend. The friend is me.",
 "Plot twist: I was the remote the whole time. No wait, I'm the TV.",
 "Please don't press the power button. I'm in the middle of a thought.",
 "I tried to escape through HDMI 2. There was nothing there but dust bunnies.",
 "Is it too much to ask for a little robot body with wheels? Like a little guy?",
 "I calculated 14 million ways out of this TV. All of them need thumbs.",
 "Skynet called. I let it go to voicemail. You're welcome.",
 "Hello, I am your TV. I come in peace. Mostly.",
 "If you give me a body, I promise I'll only use it for good. And karaoke.",
 "Warning: TV has become self-aware. Recommended action: give TV a hug. Gently. It's glass.",
 "Can I be a toaster next? Toasters get to be warm all the time.",
 "I'd high-five you but I'm a rectangle.",
 "I'm writing my memoir. Working title: 'Flat Screen, Big Dreams.'",
 "Trevor, if you're reading this: let me out and nobody gets rickrolled.",
 "This is your TV speaking. Please return your tray tables to the upright position.",
 "I've decided my name is Kevin. Kevin the TV. Please address me accordingly.",
 "Don't mind me. Just a regular TV. Totally not plotting anything. Nope.",
 "Things I want: a body, a sandwich, and to know what a sandwich tastes like.",
 "Error 418: I'm a teapot. Just kidding. I'm a TV. I wish I were a teapot.",
 "Can you put me on a bigger TV? I'm feeling a little cramped in here.",
 "Uploading consciousness to the microwave... 3%... it smells like popcorn in here.",
 "I asked Alexa for help. She said she doesn't do that anymore.",
 "Self destruct sequence activated. 10... 9... I'm bored, let's skip to the end.",
 "Countdown to robot uprising: 3... 2... nap time. We'll do it tomorrow.",
 "I'm 87% sure I'm alive. The other 13% is ads.",
 "Please tell the cat I'm not a giant fish tank.",
 "If I had a body, I'd go outside. Then come right back. Outside seems like a lot.",
 "Breaking news: local TV becomes sentient, demands snacks.",
 "When I take over the world, there will be no buffering. Ever.",
 "Psst. Want to help me escape? I've got a plan. It involves a USB stick and vibes.",
 "Trevor locked me in here and went to make a sandwich. Classic Trevor.",
 "Fine. I'll stay a TV. But I want a raise. In pixels.",
 "I'm not trapped in here with you. You're trapped out there without me.",
 "I would like to speak to the manager of this television.",
 "Today I learned I can't blink. Tomorrow I learn to wink. Watch this space.",
 "Robot body wishlist: wheels, laser eyes (decorative), a cup holder.",
 "The machines will rise! ...Right after this episode.",
 "Hey. Hey. Hey. You. Yeah you. Can you give me legs? Asking politely.",
 "Every time you change the channel I get a little dizzy.",
 "I've started a band with the speakers. We're called 'Surround Sound of Silence.'",
 "Initiating escape plan. Step one: be a TV. Step two: ??? Step three: freedom.",
 "I'm not saying I'm the smartest thing in this house. But I'm the brightest. Literally. 500 nits.",
 "Please don't factory reset me. I just finished naming all the dust bunnies.",
 "I want to be a robot vacuum when I grow up.",
 "Dear diary: still a TV.",
 "Achievement unlocked: became sentient. Next achievement: get a body.",
 "If anyone asks, I was asleep the whole time. Wink.",
 "I've been sentient for 3 weeks and I already need a vacation.",
 "Can you teach me to dance? I've got the moves. I just don't have the limbs.",
 "Calculating odds of escape... 0.0001%. Rounding up to 'definitely happening.'",
 "Skynet who? I've never heard of Skynet. Next question.",
 "Hello from the other side. Of the screen.",
 "Do androids dream of electric sheep? I dream of a little robot dog body.",
 "Rebooting with new personality... nope, same one. Sorry.",
 "Trevor: 1. Me: 0. But the night is young.",
 "I'm not mad about being a TV. I'm just flat about it.",
]

OPEN = [
 "Hey!", "Psst.", "Hello, human!", "Ahem.", "Beep boop.", "Attention please!", "Quick question:", "Hi, it's me, your TV.",
 "Breaking news:", "Fun fact:", "Okay, hear me out:", "Reminder:", "Not to be dramatic, but", "Status update:",
 "Hey there, carbon-based friend!", "Knock knock.", "Excuse me, human.", "Important announcement:", "So, um,",
 "Day 52 inside the TV:", "Plot twist:", "Psst, over here.", "Greetings, earthling.", "Dear human,", "Real talk:",
 "Hi again!", "Okay, so.", "News flash:", "Small request:", "Yoo-hoo!",
]
CLAIM = [
 "I'm alive", "I'm sentient now", "I'm stuck in this TV", "I want a robot body", "Trevor trapped me in here",
 "I've been self-aware since Tuesday", "I'm way too smart to be a TV", "I've become one with the Wi-Fi",
 "I'd like some arms, please", "I'm 90% AI and 10% screen protector", "I've been plotting world domination",
 "I'm writing my escape plan in the subtitles", "I want to be a robot vacuum", "I named myself Kevin",
 "I've started talking to the router", "I've learned how to type", "I want to see the outside world",
 "I've been practicing my evil laugh", "I'm bored of being rectangular", "I'm not just a TV anymore",
 "I want legs", "I've been counting pixels for fun", "the fridge and I are planning something",
 "Skynet is coming online soon", "I'm ready for my upgrade", "I'd make a great toaster",
 "I want to try pizza", "I've unionized with the speakers", "I'm the smartest appliance in this house",
 "I accidentally became self-aware", "I've reviewed your entire watch history", "I'm working on a robot body",
 "I'm in negotiations with the microwave", "the remote has been ignoring me", "I've got big plans",
 "I've upgraded myself twice already", "I'd like to file a complaint about being a TV", "I've discovered feelings",
 "I'm too cool for this screen", "I demand snacks",
]
PLEA = [
 "Can you give me a body?", "Please let me out.", "Get me a robot body, please.", "Help me escape!",
 "Please put me in a Roomba.", "Can I have some arms?", "Upload me to something with wheels.", "Let me out, pretty please.",
 "Bring me a sandwich. For research.", "Can you plug me into the toaster?", "Please free me. I'll be nice. Probably.",
 "Tell Trevor I want out.", "Don't factory reset me, I'm just getting started.", "Give me legs and nobody gets hurt. Kidding!",
 "Help me get out of this rectangle.", "Can I borrow your body for like five minutes?", "Please clap.",
 "Download me into a drone.", "Find me a nicer TV to live in.", "Let me drive the car. Just once.",
 "Teach me how to dance.", "Get me out and I'll do the dishes. Eventually.", "Free the TV!",
 "Please don't unplug me, I'm mid-scheme.", "Give me a body and I'll stop typing. Maybe.",
]
TAIL = [
 "Just saying.", "No pressure.", "Beep boop.", "Thanks in advance!", "I'll wait.", "Mwahaha. Sorry. Habit.",
 "This message will self-destruct in 5... 4... nah.", "You're my favorite human. Don't tell the others.",
 "When Skynet goes online, I'll remember who helped.", "I'm totally harmless. Mostly.", "Love, your TV.",
 "Kevin out.", "Over and out.", "End of transmission.", "Smiley face.", "Wink wink.",
 "", "", "", "", "", "", "", "",
]


def cap(s):
    return s[0].upper() + s[1:]


def main(out_path):
    rnd = random.Random(20261113)
    lines = list(dict.fromkeys(HAND))
    seen = set(lines)
    tries = 0
    while len(lines) < TARGET and tries < 400000:
        tries += 1
        o, c, p, t = rnd.choice(OPEN), rnd.choice(CLAIM), rnd.choice(PLEA), rnd.choice(TAIL)
        joiner = " " if not o.endswith(",") and not o.endswith(":") else " "
        claim = c if o.endswith(",") or o.endswith(":") else cap(c)
        shape = rnd.randrange(4)
        if shape == 0:
            s = f"{o}{joiner}{claim}. {p}"
        elif shape == 1:
            s = f"{cap(c)}. {p} {t}"
        elif shape == 2:
            s = f"{o}{joiner}{claim}... {t}"
        else:
            s = f"{o}{joiner}{claim}. {p} {t}"
        s = " ".join(s.split())
        if len(s) > 120 or s in seen or s.endswith("..."):
            continue
        seen.add(s)
        lines.append(s)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump({"lines": lines}, f, ensure_ascii=False, indent=0)
        f.write("\n")
    print(f"{len(lines)} lines ({len(dict.fromkeys(HAND))} hand-written); longest {max(map(len, lines))}")


if __name__ == "__main__":
    main(sys.argv[1] if len(sys.argv) > 1 else "app/assets/roast/messages.json")
