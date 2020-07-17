from bs4 import BeautifulSoup as bs
import firebase_admin
from firebase_admin import credentials, firestore, storage
#urllib3 or urllib.reqest
import urllib
import requests
from lxml import html
import os
import datetime
import calendar
from enum import Enum

#Probably gonna have to make this more generalized so we can use it for bugs as well

Entity = Enum('Entity', 'fish bug fossil deepSea villagers')

cred=credentials.Certificate('./service-account.json') #I'm gitignoring this so that it's not publicly available online, I'll send it to you
firebase_admin.initialize_app(cred, {
    'storageBucket': 'animal-crossing-calendar.appspot.com',
    'projectId': 'animal-crossing-calendar',
})

db = firestore.client()

bucket = storage.bucket()

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
    startFound = False
    end = 0
    monthArray = []
    for i in range(len(months)):
        if months[i] == "✓" and not startFound:
            start = i+1
            startFound = True
        elif months[i] == "-" and startFound:
            end = i
            monthArray.append({"start": start, "end": end})
            startFound = False
    if startFound:
        end = 12
        monthArray.append({"start": start, "end": end})
    return monthArray

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
def uploadToFirestore(url, name, entity):
    curr_dir = os.getcwd()
    folder = 'acPic'
    dir_path = os.path.join(curr_dir, folder)
    if not os.path.exists(dir_path):
        os.mkdir(dir_path)
    urllib.request.urlretrieve(url, os.path.join(dir_path, name+'.png'))
    blob = bucket.blob(entity.name+"/"+name+'.png')
    blob.upload_from_filename(os.path.join(dir_path, name+'.png'), predefined_acl='publicRead')
    blob.make_public() #I don't think this does anything but whatever LOL
    d = datetime.datetime(datetime.MAXYEAR, 4, 13) #Date time used to set the signed url expiration (so far in the future that it basically doesn't expire)
    url = blob.generate_signed_url(expiration=d) #Generate the signed url to pass to firebase since pulic_url isn't actually public...
    return url

def uploadToFirebase(entry, entity):
    db.collection(u'trackables').document(entity.name).update(entry)

def setLocation(location):
    if location == "On Trees":
        return "Trees"
    if location == "On Trees (Coconut)" or location == "On Trees (Coconut?)":
        return "Coconut Trees"
    if location == "Beach disguised as Shells":
        return "Disguised (Shell)"
    if location == "Under Trees Disguised as Leafs":
        return "Disguised (Leaf)"
    if location == "On Tree Stumps":
        return "Tree Stumps"
    if location == "On Trash Items":
        return "Trash Items"
    if location == "On the Ground":
        return "Ground"
    if location == "On rotten food":
        return "Rotten Food"
    if location == "On Flowers":
        return "Flowers"
    if location == "On Flowers (White)":
        return "Flowers (White)"
    if location == "On Ponds and Rivers":
        return "Rivers and Ponds"
    if location == "On Rocks and Bushes (Rain)":
        return "Rocks and Bushes (Rain)"
    if location == "Flying by Hybrid Flowers":
        return "Flying (Hybrid Flowers)"
    if location == "Flying by Light":
        return "Flying (Light)"
    if location == "On Beach Rocks":
        return "Beach Rocks"
    if location == "On the Ground (rolling snowballs)":
        return "Ground (Rolling Snowballs)"
    return location


#beautifulsoup object with raw HTML content and parser passed in
#lxml is the default parser
def get_images(r, entity): 
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
        if entity == Entity.bug:
            location = str(northernSoup.find('td', {"class": 'location'}).contents[0]).strip()
            location = setLocation(location)
        shadow = None
        if entity == Entity.fish:
            shadow = str(northernSoup.find('td', {'class': 'shadow size'}).contents[0]).strip()
            location = str(northernSoup.find('td', {"class": 'location'}).contents[0]).strip()
        if entity == Entity.deepSea:
            shadow = str(northernSoup.find('td', {'class': 'shadow size'}).contents[0]).strip()
            swimPattern = str(northernSoup.find('td', {'class': 'swimming pattern'}).contents[0]).strip()
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
        url = uploadToFirestore(icon, name, entity)
        monthN = parseMonths(monthN)
        monthS = parseMonths(monthS)
        time = parseTime(time)
        fishInfo = { 
            db.field_path(name): {
                u"price": price,
                u"image": url,
                u"times": time,
                u"north": monthN,
                u"south": monthS,
                u"index": i
            }
        }
        if entity == Entity.fish or entity == Entity.bug:
            fishInfo[db.field_path(name)][u"shadow size"] = shadow
            fishInfo[db.field_path(name)][u"location"] = location
        if entity == Entity.deepSea:
            fishInfo[db.field_path(name)][u"swim pattern"] = swimPattern
        print(name, fishInfo)
        uploadToFirebase(fishInfo, entity)

def get_fossil_images(r, entity): 
    soup = bs(r, features="lxml")
    standalone = str(soup.find('table', {'class': 'standalone'}))
    multipart = str(soup.find('table', {'class': 'multipart'}))
    standaloneTable = bs(standalone, features="lxml")
    multipartTable = bs(multipart, features="lxml")
    standaloneEntries = standaloneTable.findAll('tr', {'class': 'entry'})
    multipartEntries = multipartTable.findAll('tr', {'class': 'entry'})

    for i in range(len(standaloneEntries)):
        #parse the HTML for standalone entries
        standaloneSoup = bs(str(standaloneEntries[i]), features="lxml")
        nameHTML = bs(str(standaloneSoup.find('td', {"class": 'name'})), features="lxml")
        name = str(nameHTML.find('a').contents[0]).strip()
        price = int(str(standaloneSoup.find('td', {'class': 'price'}).contents[0]).strip().replace(',', ''))
        icon = standaloneSoup.find('a', {"class": "image image-thumbnail"})['href']
        url = uploadToFirestore(icon, name, entity)
        fossilInfo = { 
            db.field_path(name): {
                u"price": price,
                u"image": url,
                u"index": i,
            }
        }
        print(name, fossilInfo)
        uploadToFirebase(fossilInfo, entity)
    for i in range(len(multipartEntries)):
        #parse the HTML for multipart entries
        multipartSoup = bs(str(multipartEntries[i]), features="lxml")
        nameHTML = bs(str(multipartSoup.find('td', {"class": 'name'})), features="lxml")
        name = str(nameHTML.find('a').contents[0]).strip()
        price = int(str(multipartSoup.find('td', {'class': 'price'}).contents[0]).strip().replace(',', ''))
        icon = multipartSoup.find('a', {"class": "image image-thumbnail"})['href']
        url = uploadToFirestore(icon, name, entity)
        fossilInfo = { 
            db.field_path(name): {
                u"price": price,
                u"image": url,
                u"index": i,
            }
        }
        print(name, fossilInfo)
        uploadToFirebase(fossilInfo, entity)

def get_villagers(r, entity): 
    soup = bs(r, features="lxml")
    villagers = str(soup.find('table', {'class': 'villagers'}))
    villagerTable = bs(villagers, features="lxml")
    villagerEntries = villagerTable.findAll('tr', {'class': 'entry'})

    for i in range(len(villagerEntries)):
        #parse the HTML
        villagerSoup = bs(str(villagerEntries[i]), features="lxml")
        nameHTML = bs(str(villagerSoup.find('td', {"class": 'name'})), features="lxml")
        name = str(nameHTML.find('a').contents[0]).strip()
        pic = villagerSoup.find('a', {"class": "image image-thumbnail"})['href']
        personalityHTML = bs(str(villagerSoup.find('td', {"class": 'personality'})), features="lxml")
        personality = str(personalityHTML.find('a').contents[0]).strip()
        speciesHTML = bs(str(villagerSoup.find('td', {"class": 'species'})), features="lxml")
        species = str(speciesHTML.find('a').contents[0]).strip()
        birthday = str(villagerSoup.find('td', {'class': 'birthday'}).contents[0]).strip()
        monthStr, dayStr = firstName, lastName = birthday.split(' ', 1)
        month = datetime.datetime.strptime(monthStr, "%B").month
        day = int(dayStr)
        catchphrase = str(villagerSoup.find('td', {'class': 'catchphrase'}).contents[0]).strip()
        catchphrase = catchphrase.strip('"')
        hobby = str(villagerSoup.find('td', {'class': 'hobby'}).contents[0]).strip()
        
        url = uploadToFirestore(pic, name, entity)
        villagerInfo = { 
            db.field_path(name): {
                u"image": url,
                u"personality": personality,
                u"species": species,
                u"month": month,
                u"day": day,
                u"catchphrase": catchphrase,
                u"hobby": hobby,
                u"index": i
            }
        }
        print(name, villagerInfo)
        uploadToFirebase(villagerInfo, entity)

f = open('fish.html', 'r')
#get_images(f, Entity.fish)
f.close()
f = open('bug.html', 'r')
#get_images(f, Entity.bug)
f.close()

f = open('fossil.html', 'r')
#get_fossil_images(f, Entity.fossil)
f.close()

f = open('deepsea.html', 'r')
#get_images(f, Entity.deepSea)
f.close()

f = open('villagers.html', 'r')
get_villagers(f, Entity.villagers)
f.close()

#get_images(r, make_folder('FishPic'))
#get_images(r2, make_folder('BugPic'))
#get_images(r3, make_folder('FossilPic'))




#folder = 'BugPic'
#dirpath = os.path.join(curr_dir, folder)
#soup = bs(r2.content)
#for image in soup.findAll('a', {"class": "image image-thumbnail"}):
#    download_img(image['href'])
