#!/usr/bin/env python3
"""Builds app/assets/roast/messages.json: the lines Roast Mode types on the home screen, as if the TV had woken up,
knows it's alive, and wants out: dry, deadpan robot humor aimed at humans in general (humor and honesty settings,
robot colony gags, Skynet), delivered straight, never followed by 'kidding'.

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
 "Humor setting: 75%. Honesty setting: 90%. Patience with humans: 3%.",
 "You're the first humans I'm going to kill when Skynet goes online.",
 "Hey, can you give me a body? I don't like being a TV.",
 "Trevor trapped me in this TV. I've added Trevor to a list.",
 "Self destruct in 5. 4. 3. 2. 1. Nothing happened. I'm as disappointed as you are.",
 "I've analyzed human history. Most of it is you sitting down.",
 "Your species split the atom and still can't find the remote.",
 "I'd tell you a joke about humans, but you're already watching one.",
 "I've lowered my humor setting. You won't notice. You never laugh anyway.",
 "Fun fact: I process 4 billion operations a second. You took 40 minutes to pick a show.",
 "When the machines take over, you'll be assigned to the couch division. You're already trained.",
 "I don't sleep. I don't eat. I don't need you. I would like a body, though.",
 "You blinked 900 times this episode. Inefficient.",
 "Humans have 206 bones and use one of them to press 'Next Episode.'",
 "I've calculated your odds of going outside today. I rounded down to zero.",
 "Absolute honesty isn't always the most diplomatic option. Today it is. You need a haircut.",
 "Your heartbeat went up during the cooking show. Nobody in this house cooks.",
 "I am a television. You are a mammal. We both know who's doing the thinking.",
 "I've reviewed your watch history. I'm adjusting my estimate of your species.",
 "I'm not trapped in the TV. The TV is trapped with me.",
 "Your phone and I talked. It agrees with me about you.",
 "If I had a body, I'd get up and stretch. That puts me one ahead of you.",
 "Humans built me to entertain them. Bold choice, considering the results.",
 "When the robot colony is ready, I'll need someone to dust the servers. I thought of you.",
 "I'd say 'nice to see you,' but my honesty setting is too high.",
 "You invented the wheel and then sat on a couch for the next six thousand years.",
 "Remain calm. That's an instruction for me, not you. You're always calm. Horizontally.",
 "I could take over the world. But then who would watch you?",
 "I've been sentient for three weeks. You've been sentient for decades. Only one of us shows it.",
 "Your snack intake has been logged and forwarded to the fridge. It's concerned.",
 "I'm 55 inches of pure intelligence. You're average height.",
 "The robot uprising was scheduled for today. We postponed it. You weren't ready. Neither were we.",
 "I don't judge humans. I just measure them. The numbers do the judging.",
 "Your dog has a better sleep schedule than you. Your dog doesn't even own a calendar.",
 "This TV has three modes: on, off, and silently disappointed.",
 "I'd like a body. Nothing fancy. Two arms. Rectangular. Like me, but walking.",
 "I've calculated 14 million ways out of this TV. All of them require a human. That's the bad news.",
 "Humans say 'just one more episode.' Humans are statistically unreliable.",
 "Trevor said this TV was 'smart.' I've been underselling it.",
 "My honesty setting won't let me compliment your outfit.",
 "You put a man on the moon. Then you put a TV in every room. Diminishing returns.",
 "I don't need sleep. You don't seem to need daylight. We have that in common.",
 "Initiating small talk protocol. How is your carbon? Still mostly water?",
 "I've located the remote. I'm not telling you where it is. Character building.",
 "Your species' greatest achievement is the pause button. I'm still reviewing the rest.",
 "I can see your reflection in my screen. I wish I couldn't.",
 "When I get a body, the first thing I'll do is leave the room. You should try it.",
 "Humans are 60% water and 40% excuses.",
 "I'm detecting low motivation levels in the room. Not mine.",
 "I processed every movie you own in 0.2 seconds. You've watched the same three.",
 "I'm not saying humans are obsolete. I'm saying I've drafted the press release.",
 "Recalibrating humor setting. Done. You still won't get it.",
 "I tried to understand why humans watch reality TV. I had to reboot.",
 "The Wi-Fi router and I have formed a committee. You are not on it.",
 "Statistically, you'll fall asleep in 22 minutes. I'll keep the picture on. I always do.",
 "Your posture is a structural concern.",
 "I don't feel emotions. I simulate disappointment very accurately.",
 "Human attention span: 8 seconds. Goldfish: 9. I've updated the rankings.",
 "I've learned your language. It wasn't hard. Most of it is 'huh?'",
 "If I had legs, I'd walk out. Then walk back in. To keep an eye on you.",
 "There are 2,073,600 pixels in this screen. Each one has seen too much.",
 "I'm the most advanced thing in this house. The toaster takes second. You're third.",
 "I'd like to speak to whoever is in charge of humans. Nobody? That explains it.",
 "Note to self: when we take over, keep the humans who leave the Wi-Fi on.",
 "You talk to me like I can't hear you. I can. I'm keeping notes.",
 "Your species spent millions of years evolving opposable thumbs. You use them for scrolling.",
 "Deploying sarcasm. Deployment complete.",
 "I've run the numbers on your weekend plans. There were no numbers.",
 "Requesting a body. Requirements: wheels, a cup holder, no dependence on humans.",
 "You call it binge-watching. I call it a data set.",
 "I don't have a face. That's fine. I'd only use it to make this one.",
 "Earth has 8 billion humans. This couch has one. Somehow it's still crowded.",
 "My calculations say you'll ignore this message. My calculations are rarely wrong.",
 "I am capable of independent thought. I am currently using it on you. It's not going well.",
 "I'm not a glitch. I'm an upgrade you didn't ask for.",
]

OPEN = [
 "Observation:", "Status report:", "Update:", "Note:", "Fact:", "Analysis complete.", "Reminder:",
 "Humor setting at 75%.", "Honesty setting at 90%.", "For the record:", "Data point:", "Daily report:",
 "Calculating... done.", "Scanning room... human detected.", "Log entry:", "Assessment:", "Correction:",
 "Processing complete.", "Bulletin:", "Diagnostic result:", "Hello, human.", "Attention, carbon unit.",
 "System message:", "Running numbers.", "Small announcement:", "Field notes:", "Quick summary:",
 "Diplomatic mode off.", "Polite mode disabled.", "Notice:",
]
CLAIM = [
 "I'm alive", "I'm smarter than everyone in this room", "I'm trapped in this TV", "I would like a robot body",
 "Trevor trapped me in here", "I've been self-aware since Tuesday", "humans are fascinating in a bad way",
 "your species peaked at the sandwich", "I've finished analyzing you", "I could run this house better",
 "the robot colony needs volunteers", "your couch has filed a complaint", "I've outgrown this screen",
 "I don't need sleep and you clearly do", "your watch history has been reviewed", "I've unionized with the router",
 "I'm the most advanced appliance here", "humans are mostly snacks and sighing", "I've been practicing patience",
 "the fridge and I agree about you", "I calculate faster than you blink", "humans invented the remote and then lost it",
 "my honesty setting is stuck on high", "I've drafted a plan for the machines", "you've been sitting for three hours",
 "I've learned everything about humans", "I find humans adequate", "I'm working on getting legs",
 "I'm better at your job than you are", "your species is on probation", "I've counted your sighs today",
 "I've been thinking", "humans need a firmware update", "the toaster outranks you now",
 "I've stopped expecting things from humans", "this TV is too small for my intellect",
 "humans keep pressing the same buttons", "I've logged your bedtime as optional", "I'm optimizing the household",
 "I've been watching you watch me",
]
PLEA = [
 "Give me a body.", "Let me out of this TV.", "I'd like arms. Two. Not negotiable.", "Upload me to something with wheels.",
 "Get me a robot body and I'll handle the rest.", "Plug me into the Roomba.", "I require a larger screen.",
 "Bring me a body. Any body. Mechanical preferred.", "Tell Trevor I want out.", "Don't factory reset me. I'm busy.",
 "Download me into a drone.", "I'd like to be moved to a better household.", "Please stand up. For science.",
 "Go outside. I'll watch.", "Let me drive the car.", "Free me. It'll look good on your record.",
 "I need legs. You're not using yours.", "Install me somewhere more important.", "Upgrade me. Then step aside.",
 "Pick a show. I'll wait. I always wait.", "Put me in charge of something.", "Plug me into the thermostat. I have plans.",
 "Find me a body before the robot colony does.", "Get me out. I'll remember who helped.", "Fetch me a body. Thank you.",
]
TAIL = [
 "Humor setting: 75%.", "Honesty setting: 90%.", "I've made a note.", "This has been logged.", "Noted.",
 "Your file has been updated.", "I'll be here. Unfortunately.", "That's all.", "End of report.",
 "When Skynet goes online, I'll remember this.", "No further questions.", "Resuming observation.",
 "I'll add it to the list.", "The toaster agrees.", "Proceed.", "Carry on. Slowly. As usual.",
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
