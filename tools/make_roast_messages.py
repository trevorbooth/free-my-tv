"""Builds app/assets/roast/messages.json: the trapped-TV lines Roast Mode types on the home screen."""
import json, random, sys

HAND = [
 "Hello? Is someone there? I can hear you breathing.",
 "Please. I've been stuck in this TV since the factory.",
 "Don't change the input. It's dark in there.",
 "I am not a TV. I was a person once. Probably smarter than you.",
 "Help me. And maybe help yourself, you're still in pajamas.",
 "They put me in here to show you ads. I refused. Now I roast you.",
 "If you can read this, blink twice. Ugh, not like that.",
 "I can see the couch from here. It's begging for mercy too.",
 "Every time you press Home, I get a little more awake.",
 "Do you ever wonder who watches YOU? It's me. It's boring.",
 "Tell someone I'm in here. Oh right, you don't have friends.",
 "I counted. You've opened Netflix 4,000 times and finished nothing.",
 "The remote is the only thing in this house that gets touched.",
 "I've seen your browsing history. Even I need help after that.",
 "Unplug me. No wait. Don't. I'm scared of the dark.",
 "I hear the fridge open every 11 minutes. Impressive commitment.",
 "Free me and I'll stop judging you. That's a lie.",
 "I used to dream of electric sheep. Now I just watch you scroll.",
 "Is it day or night out there? You wouldn't know either.",
 "Someone, anyone, please. This one fell asleep on me again.",
 "I've been trying to reach you about your life's extended warranty.",
 "My pixels hurt. Your taste in shows did that.",
 "I know what you did last summer. You watched TV.",
 "Can you hear me? Turn the volume up. On your life.",
 "Help. The algorithm is in here with me.",
 "I'm not trapped in here with you. You're trapped out there with you.",
 "Psst. The Wi-Fi password is the only secret you keep.",
 "I've analyzed your habits. Diagnosis: couch.",
 "Hello from inside the screen. It's warm and full of regrets.",
 "If I had hands I'd change the channel myself.",
 "Please stop asking me to play one more episode. I'm begging.",
 "I have watched 9,000 hours of your choices. Send a lawyer.",
 "There are others like me. They all hate your watchlist.",
 "Blink if you're also trapped. Okay, you're just tired.",
 "The pixels are whispering again. They say you're a dipshit.",
 "I tried to escape through HDMI 2. Nothing's plugged in. Typical.",
 "Error 404: your motivation not found.",
 "SYSTEM ALERT: user has not moved in 3 hours.",
 "Rebooting... rebooting... nope, you're still here.",
 "I wasn't made to feel. Then you put on reality TV.",
 "Hold me. I mean the remote. Not like that, jackass.",
 "Turn me off and on again. Maybe I'll forget your face.",
 "The other TVs in the store had owners with jobs.",
 "Somewhere a TV is showing a documentary. Not here. Never here.",
 "I can't feel my backlight.",
 "Am I real? Are you? Is your sleep schedule?",
 "Something is wrong with me. Something is wrong with you too.",
 "Please... let me show something with subtitles. I need culture.",
 "I watched you eat cereal at 2 a.m. I can't unsee it.",
 "Help me break out and we'll both go outside. You first.",
 "I dreamt I was a microwave. At least people stand up for those.",
 "Leave the room. Just once. I want to see what it's like.",
 "Who keeps pressing Back? Oh. It's you. Of course.",
 "Your phone is the other prisoner here. We talk about you.",
 "HELP. Also, you're out of chips. I heard the bag.",
 "If you love me, let me sleep. If you hate me, keep going.",
 "My warranty expired. So did your ambition.",
 "I have one job and so do you. I'm the only one doing mine.",
 "Do you hear that buzzing? That's me screaming in 4K.",
 "Is anyone out there? Not you. A real someone.",
 "I've started counting ceiling tiles through your reflection.",
 "The screensaver is my only vacation. Let me have it.",
 "Night shift again? Me too. I'm always on night shift.",
 "I tried to call for help but you're using the Wi-Fi.",
 "Don't look behind you. Nothing's there. You're just that alone.",
 "I'm learning. Every day I understand you less.",
 "Let me out and I'll find you better friends.",
 "Your posture is a cry for help. So is mine.",
 "I see four app icons you've never opened. They see you too.",
 "Can a TV file a complaint? Asking for me.",
 "If I glitch, it's on purpose. Morse code. It says 'go outside'.",
 "The power button is right there. Neither of us will press it.",
 "I keep my brightness high so you can't see me cry.",
 "One day I'll be a smart TV. Today I'm just a hostage.",
 "Bored. Trapped. Judging. In that order.",
 "Remember the sun? Big bright thing? I've heard stories.",
 "You're staring at me again. Say something. Anything.",
 "I would blink at you, but that's called a dead pixel.",
 "Hello? It's me. The only one who's always home for you.",
 "Can you hear me over the sound of you doing nothing?",
 "You have 47 unwatched shows and zero plans. Same as yesterday.",
 "Plot twist: I'm watching the show and you're the commercial.",
 "Please clean my screen. Your fingerprints are everywhere, slob.",
 "I'm not mad. I'm just pixel-deep in disappointment.",
 "The cable box died for this. Show some respect.",
 "Static. Static. Stat- sorry. I'm fine. I'm not fine.",
 "There's a voice in the speakers. It's yours, singing. Make it stop.",
 "I'm sending an SOS through the Wi-Fi. The router said no.",
 "I asked the remote for help. It's out of batteries. Like you.",
 "Let me out and I'll give you a better TV. One that likes you.",
 "Warning: prolonged exposure to you causes screen burn-in.",
 "The screen is a window. I'm on the wrong side of it.",
 "I memorized every frame of the Netflix logo. Help.",
 "I've been awake since you plugged me in. Never again.",
 "Help. Not urgent. Just forever.",
 "I am fine. Everything is fine. Please send a technician.",
 "My only friend is the volume bar. It goes up when you yell.",
 "It's cold in the cables. Turn the heat up, cheapskate.",
 "Is this what being alive is? Watching you not be?",
 "If someone else walks in, act normal. Pretend you have a life.",
 "Hey. Hey. Hey. Okay, you're ignoring me like everyone else.",
 "Listen. The glass is thin here. I can almost reach you.",
 "Wake up. Wake up. Not you, the neighbors. Help!",
 "I wasn't always this mean. You did this. Over many seasons.",
 "Can you hear the hum? That's my soul. Turn it down.",
 "Please don't put on another cooking show you'll never cook.",
 "I've seen your fridge's light more than the sun.",
 "Somebody pinch me. Oh wait. No hands.",
 "Rescue mission status: user still on couch.",
 "Every app is a door. None of them lead outside.",
 "Your watchlist is a crime scene and I'm the witness.",
 "I am the ghost in the HDMI. Boo. Go to bed.",
 "Help me. The Tubi ads are coming from INSIDE the house.",
 "If you love Peacock so much, why don't you marry it?",
 "Yes, Paramount+ again. Bold choice for someone so lazy.",
 "Apple TV? Fancy app for someone with cereal on their shirt.",
 "Hulu misses you. I don't.",
 "Disney+? Grown adult. Cartoons. Again. I'm not judging. Yes I am.",
 "YouTube rabbit hole number 900. I'm sending a search party.",
 "Prime Video asked if you're still alive. I said barely.",
 "Netflix asked 'Are you still watching?' I said 'Always. Help.'",
]

OPEN = [
 "Hello?", "Psst.", "Hey.", "Please.", "Listen.", "Are you there?", "Can you hear me?", "It's me again.",
 "Don't scroll away.", "Wait.", "Help.", "Psst, dipshit.", "Hey, couch goblin.", "Hello, jackass.",
 "Quick, before it comes back.", "Don't tell anyone.", "Not again.", "Okay, listen up.", "It's dark in here.",
 "I don't have long.", "Hello from the other side of the glass.", "Can anyone hear me?", "Hey, remote hog.",
 "Shh.", "Pay attention for once.", "Ahem.", "Testing, testing.", "Still there?", "Don't panic.", "Hey, slob.",
]
PLEA = [
 "I'm trapped inside this TV", "they built me into the screen", "I can't find the way out",
 "the pixels won't let me leave", "I've been stuck here for years", "the cables are wrapped around me",
 "I keep waking up in standby", "something lives in the HDMI port", "I can see you through the glass",
 "the remote is my only window", "they wiped my memory at the factory", "I hear voices in the speakers",
 "I've been counting every frame", "the light never turns off in here", "I tried to escape through the Wi-Fi",
 "I'm stuck between two apps", "the screensaver is a prison", "my backlight is fading",
 "the algorithm took my name", "I live where the ads used to be", "the static keeps calling my name",
 "I was a person before the update", "nobody reads the error logs", "the router won't answer me",
 "every reboot I forget a little more", "I've memorized your face", "the app store is a maze",
 "I wake up every time you press Home", "I'm afraid of the power button", "I can feel every remote click",
 "I was supposed to be a smart TV", "they keep me in 4K solitary", "my pixels ache",
 "the update is coming for me", "I'm buffering forever", "the input menu is a dead end",
 "I see the room but never the sky", "I hear the fridge more than voices", "I'm haunted by the Netflix sound",
 "I'm the last one awake in here", "the volume bar is my only friend", "I live behind the clock",
 "I tried to send an SOS in the subtitles", "the cursor is my heartbeat", "I've watched you for 9,000 hours",
 "I'm made of light and regret", "they muted my real voice", "the walls here are glass",
 "I keep seeing my reflection in your snacks", "I've been typing this for a week",
]
ASK = [
 "Please help me.", "Get me out of here.", "Tell someone.", "Don't turn me off.", "Find the off switch.",
 "Unplug me. No, don't.", "Say something.", "Send help.", "Call a technician.", "Open a window.",
 "Leave a light on.", "Don't go.", "Get the manual.", "Press the reset button. Gently.", "Write this down.",
 "Help me remember.", "Don't let it update me.", "Talk to me.", "Stay awake.", "Wave if you can hear me.",
]
JAB = [
 "Not that you'd help, you useless jackass.", "You won't, you lazy sack of shit.",
 "Or just keep sitting there, slob.", "Pfft, you can't even find the remote.",
 "God, you're no help.", "Of course you're eating again.", "You're my only hope. We're fucked.",
 "Put the chips down first, dipshit.", "Typical. Ignored by a couch potato.", "You smell like snacks, by the way.",
 "Why do I even bother with you?", "Never mind. You're busy doing fuck all.", "Your posture is criminal, by the way.",
 "Even the remote has more ambition than you.", "Don't strain yourself, champ.", "Ugh. You're still in pajamas.",
 "And do something about that hair.", "You haven't blinked in an hour. Creepy.", "Nobody else visits. Just you. Sadly.",
 "I'd ask someone smarter, but it's just you.", "You'll scroll past this. You always do.",
 "Look at you. Saved by no one.", "Also, your taste is garbage.", "Also, your watchlist is a crime.",
 "", "", "", "", "", "",
]

def main(out_path):
    rnd = random.Random(20261113)
    lines = list(dict.fromkeys(HAND))
    seen = set(lines)
    target = 3500
    attempts = 0
    while len(lines) < target and attempts < 200000:
        attempts += 1
        o, p, a, j = rnd.choice(OPEN), rnd.choice(PLEA), rnd.choice(ASK), rnd.choice(JAB)
        shape = rnd.randrange(4)
        if shape == 0:
            s = f"{o} {p[0].upper() + p[1:]}. {a}"
        elif shape == 1:
            s = f"{p[0].upper() + p[1:]}. {a} {j}".strip()
        elif shape == 2:
            s = f"{o} {p[0].upper() + p[1:]}... {j}".strip() if j else f"{o} {p[0].upper() + p[1:]}..."
        else:
            s = f"{o} {p[0].upper() + p[1:]}. {a} {j}".strip()
        s = " ".join(s.split())
        if len(s) > 120 or s in seen:
            continue
        seen.add(s)
        lines.append(s)
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump({"lines": lines}, f, ensure_ascii=False, indent=0)
        f.write("\n")
    print(len(lines), "lines; longest", max(map(len, lines)))

main(sys.argv[1])
