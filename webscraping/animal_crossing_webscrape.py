from bs4 import BeautifulSoup as bs
import firebase_admin
from firebase_admin import credentials, firestore, storage
#urllib3 or urllib.reqest
import urllib
import requests
from lxml import html
import os
import datetime

#Probably gonna have to make this more generalized so we can use it for bugs as well

cred=credentials.Certificate('./animal-crossing-calendar-firebase-adminsdk-8on8r-e5ebda36ce.json')
firebase_admin.initialize_app(cred, {
    'storageBucket': 'animal-crossing-calendar.appspot.com',
    'projectId': 'animal-crossing-calendar',
})

db = firestore.client()

bucket = storage.bucket()

"""
fish = 'https://animalcrossing.fandom.com/wiki/Fish_(New_Horizons)'
#getting the webpage
r = requests.get(fish)

bug = 'https://animalcrossing.fandom.com/wiki/Bugs_(New_Horizons)'
r2 = requests.get(bug)

fossil = 'https://animalcrossing.fandom.com/wiki/Fossils_(New_Horizons)'
r3 = requests.get(fossil)

tools = 'https://animalcrossing.fandom.com/wiki/DIY_recipes/Tools'
r4 = requests.get(tools)

housewares = 'https://animalcrossing.fandom.com/wiki/DIY_recipes/Housewares'
r5 = requests.get(housewares)

miscellaneous = 'https://animalcrossing.fandom.com/wiki/DIY_recipes/Miscellaneous'
r6 = requests.get(miscellaneous)

wall_mounted = 'https://animalcrossing.fandom.com/wiki/DIY_recipes/Wall-mounted'
r7 = requests.get(wall_mounted)

#we probably want to separate this into separate ones
#there are pages that separate these but some seem to be under construction
wallpaper_rugs_flooring = 'https://animalcrossing.fandom.com/wiki/DIY_recipes/Wallpaper,_rugs_and_flooring'
r8 = requests.get(wallpaper_rugs_flooring)

equipment = 'https://animalcrossing.fandom.com/wiki/DIY_recipes/Equipment'
r9 = requests.get(equipment)

other = 'https://animalcrossing.fandom.com/wiki/DIY_recipes/Other'
r10 = requests.get(other)

art = 'https://animalcrossing.fandom.com/wiki/Art_(New_Horizons)'
r11 = requests.get(art)

tops = 'https://animalcrossing.fandom.com/wiki/Clothing_(New_Horizons)/Tops'
r12 = requests.get(tops)

bottoms = 'https://animalcrossing.fandom.com/wiki/Clothing_(New_Horizons)/Bottoms'
r13 = requests.get(bottoms)

dresses = 'https://animalcrossing.fandom.com/wiki/Clothing_(New_Horizons)/Dresses'
r14 = requests.get(dresses)

hats = 'https://animalcrossing.fandom.com/wiki/Clothing_(New_Horizons)/Hats'
r15 = requests.get(hats)

accessories = 'https://animalcrossing.fandom.com/wiki/Clothing_(New_Horizons)/Accessories'
r16 = requests.get(accessories)

socks = 'https://animalcrossing.fandom.com/wiki/Clothing_(New_Horizons)/Socks'
r17 = requests.get(socks)

shoes = 'https://animalcrossing.fandom.com/wiki/Clothing_(New_Horizons)/Shoes'
r18 = requests.get(shoes)

bags = 'https://animalcrossing.fandom.com/wiki/Clothing_(New_Horizons)/Bags'
r19 = requests.get(bags)

umbrellas = 'https://animalcrossing.fandom.com/wiki/Clothing_(New_Horizons)/Umbrellas'
r20 = requests.get(umbrellas)
"""

#We need to probably either get rid of stuff to avoid repetition
#or just make it so that if one is checked off in one activity, it's also marked as true in the other

dir_path = ''

#creating the directory to store downloaded images
def make_folder(name):
    curr_dir = os.getcwd()
    dir_path = os.path.join(curr_dir, name)
    if not os.path.exists(dir_path):
        os.mkdir(dir_path)
    return dir_path

#parse the months from the html...this function has to change... :(
def parseMonths(months):
    start = 0
    potenStartFound = False
    end = 0
    endFound = False
    for i in range(len(months)):
        if months[i] == "✓" and not potenStartFound:
            start = i+1
            potenStartFound = True
        elif months[i] == "-" and potenStartFound and not endFound:
            end = i
            endFound = True
        elif months[i] == "✓" and endFound:
            start = i+1
    if not endFound:
        end = 12
    return {"start": start, "end": end}

#Parses the time string from html and returns an array of start and end times the fish is available
def parseTime(timeStr):
    if timeStr == 'All day':
        return [{"start": 0, "end": 24}]
    #remove the unnecessary characters
    timeStr = timeStr.replace('-', '')
    timeStr = timeStr.replace('&', '')
    split = timeStr.split()
    i = 0
    timeArr = []
    #split array formatted: time, AM, time, PM, repeat
    while i < len(split) :
        start = int(split[i])
        i+=1
        if split[i] == 'PM':
            start += 12
        i+=1
        end = int(split[i])
        i+=1
        if split[i] == 'PM':
            end += 12
        timeArr.append({"start": start, "end": end})
        i+=1
    return timeArr

#download the image from the url to local then upload the local file to firebase
def uploadToFirestore(url, name):
    curr_dir = os.getcwd()
    folder = 'acPic'
    dir_path = os.path.join(curr_dir, folder)
    if not os.path.exists(dir_path):
        os.mkdir(dir_path)
    urllib.request.urlretrieve(url, os.path.join(dir_path, name+'.png'))
    blob = bucket.blob("fish/"+name+'.png')
    blob.make_public() #I don't think this does anything but whatever LOL
    blob.upload_from_filename(os.path.join(dir_path, name+'.png'), predefined_acl='publicRead')
    d = datetime.datetime(datetime.MAXYEAR, 4, 13) #Date time used to set the signed url expiration (so far in the future that it basically doesn't expire)
    url = blob.generate_signed_url(expiration=d) #Generate the signed url to pass to firebase since pulic_url isn't actually public...
    return url

def uploadToFirebase(entry):
    db.collection(u'trackables').document(u'fish').update(entry)

#beautifulsoup object with raw HTML content and parser passed in
#lxml is the default parser
def get_images(r): 
    soup = bs(r, features="lxml")
    northern = str(soup.find('table', {'class': 'northern'}))
    southern = str(soup.find('table', {'class': 'southern'}))
    northTable = bs(northern, features="lxml")
    southTable = bs(southern, features="lxml")
    northernEntries = northTable.findAll('tr', {'class': 'entry'})
    southernEntries = southTable.findAll('tr', {'class': 'entry'})

    for i in range(len(northernEntries)):
        #parse the HTML
        northernSoup = bs(str(northernEntries[i]), features="lxml")
        southernSoup = bs(str(southernEntries[i]), features="lxml")
        nameHTML = bs(str(northernSoup.find('td', {"class": 'name'})), features="lxml")
        name = str(nameHTML.find('a').contents[0]).strip()
        price = int(str(northernSoup.find('td', {'class': 'price'}).contents[0]).strip().replace(',', ''))
        icon = northernSoup.find('a', {"class": "image image-thumbnail"})['href']
        location = str(northernSoup.find('td', {"class": 'location'}).contents[0]).strip()
        shadow = str(northernSoup.find('td', {'class': 'shadow size'}).contents[0]).strip()
        time = str(northernSoup.find('td', {'class': 'time'}).contents[0]).strip()
        janN = str(northernSoup.find('td', {'class': 'jan'}).contents[0]).strip()
        febN = str(northernSoup.find('td', {'class': 'feb'}).contents[0]).strip()
        marN = str(northernSoup.find('td', {'class': 'mar'}).contents[0]).strip()
        aprN = str(northernSoup.find('td', {'class': 'apr'}).contents[0]).strip()
        mayN = str(northernSoup.find('td', {'class': 'may'}).contents[0]).strip()
        junN = str(northernSoup.find('td', {'class': 'jun'}).contents[0]).strip()
        julN = str(northernSoup.find('td', {'class': 'jul'}).contents[0]).strip()
        augN = str(northernSoup.find('td', {'class': 'aug'}).contents[0]).strip()
        sepN = str(northernSoup.find('td', {'class': 'sep'}).contents[0]).strip()
        octN = str(northernSoup.find('td', {'class': 'oct'}).contents[0]).strip()
        novN = str(northernSoup.find('td', {'class': 'nov'}).contents[0]).strip()
        decN = str(northernSoup.find('td', {'class': 'dec'}).contents[0]).strip()
        janS = str(southernSoup.find('td', {'class': 'jan'}).contents[0]).strip()
        febS = str(southernSoup.find('td', {'class': 'feb'}).contents[0]).strip()
        marS = str(southernSoup.find('td', {'class': 'mar'}).contents[0]).strip()
        aprS = str(southernSoup.find('td', {'class': 'apr'}).contents[0]).strip()
        mayS = str(southernSoup.find('td', {'class': 'may'}).contents[0]).strip()
        junS = str(southernSoup.find('td', {'class': 'jun'}).contents[0]).strip()
        julS = str(southernSoup.find('td', {'class': 'jul'}).contents[0]).strip()
        augS = str(southernSoup.find('td', {'class': 'aug'}).contents[0]).strip()
        sepS = str(southernSoup.find('td', {'class': 'sep'}).contents[0]).strip()
        octS = str(southernSoup.find('td', {'class': 'oct'}).contents[0]).strip()
        novS = str(southernSoup.find('td', {'class': 'nov'}).contents[0]).strip()
        decS = str(southernSoup.find('td', {'class': 'dec'}).contents[0]).strip()
        
        monthN = [janN, febN, marN, aprN, mayN, junN, julN, augN, sepN, octN, novN, decN]
        monthS = [janS, febS, marS, aprS, mayS, junS, julS, augS, sepS, octS, novS, decS]
        url = uploadToFirestore(icon, name)
        monthN = parseMonths(monthN)
        monthS = parseMonths(monthS)
        time = parseTime(time)
        fishInfo = { 
            db.field_path(name): {
                u"price": price,
                u"image": url,
                u"location": location,
                u"shadow size": shadow,
                u"times": time,
                u"north": {u"start month": monthN["start"], u"end month": monthN["end"]},
                u"south": {u"start month": monthS["start"], u"end month": monthS["end"]}
            }
        }
        print(name, fishInfo)
        uploadToFirebase(fishInfo)

f = open('fish.html', 'r')
get_images(f)
#get_images(r, make_folder('FishPic'))
#get_images(r2, make_folder('BugPic'))
#get_images(r3, make_folder('FossilPic'))




#folder = 'BugPic'
#dirpath = os.path.join(curr_dir, folder)
#soup = bs(r2.content)
#for image in soup.findAll('a', {"class": "image image-thumbnail"}):
#    download_img(image['href'])
